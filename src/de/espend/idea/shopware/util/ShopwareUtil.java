package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl;
import com.jetbrains.smarty.SmartyFile;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PsiElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopwareUtil {

    public static void writeShopwareMagicFile(String outputString, String outputPath) {

        File file = new File(outputPath);

        // create .idea folder, should not occur
        File folder = new File(file.getParent());
        if(!folder.exists() && !folder.mkdir()) {
            return;
        }

        FileWriter fw;
        try {
            fw = new FileWriter(file);
            fw.write(outputString);
            fw.close();
        } catch (IOException ignored) {
        }

    }

    public static void collectControllerClass(Project project, ControllerClassVisitor controllerClassVisitor) {

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> phpClasses = phpIndex.getAllSubclasses("\\Enlight_Controller_Action");

        Pattern pattern = Pattern.compile(".*_(Frontend|Backend|Core)_(\\w+)");

        for (PhpClass phpClass : phpClasses) {

            String className = phpClass.getName();
            Matcher matcher = pattern.matcher(className);

            if(matcher.find()) {
                String moduleName = matcher.group(1);
                String controller = matcher.group(2);
                controllerClassVisitor.visitClass(phpClass, moduleName, controller);
            }

        }

    }

    public interface ControllerClassVisitor {
        public void visitClass(PhpClass phpClass, String moduleName, String controllerName);
    }

    public static void collectControllerActionSmartyWrapper(PsiElement psiElement, ControllerActionVisitor visitor) {

        Pattern pattern = Pattern.compile("controller=[\"|']*(\\w+)[\"|']*");
        Matcher matcher = pattern.matcher(psiElement.getParent().getText());
        if(!matcher.find()) {
            return;
        }

        String controllerName = toCamelCase(matcher.group(1), false);
        collectControllerAction(psiElement.getProject(), controllerName, visitor);
    }

    public static void collectControllerAction(Project project, String controllerName, ControllerActionVisitor visitor) {

        for(String moduleName: new String[] {"Frontend", "Backend", "Core"}) {
            PhpClass phpClass = PhpElementsUtil.getClass(project, String.format("Shopware_Controllers_%s_%s", moduleName, controllerName));
            if(phpClass != null) {
                for(Method method: phpClass.getMethods()) {
                    if(method.getAccess().isPublic() && method.getName().endsWith("Action")) {
                        visitor.visitMethod(method, method.getName().substring(0, method.getName().length() - 6), moduleName, controllerName);
                    }
                }
            }
        }

    }

    public interface ControllerActionVisitor {
        public void visitMethod(Method method, String methodStripped, String moduleName, String controllerName);
    }

    @Nullable
    public static Method getControllerActionOnSmartyFile(SmartyFile smartyFile) {

        String relativeFilename = VfsUtil.getRelativePath(smartyFile.getVirtualFile(), smartyFile.getProject().getBaseDir(), '/');
        if(relativeFilename == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(".*/(frontend|backend|core)/(\\w+)/(\\w+)\\.tpl");
        Matcher matcher = pattern.matcher(relativeFilename);

        if(!matcher.find()) {
            return null;
        }

        // Shopware_Controllers_Frontend_Account
        String moduleName = toCamelCase(matcher.group(1), false);
        String controller = toCamelCase(matcher.group(2), false);
        String action = toCamelCase(matcher.group(3), true);

        // build class name
        String className = String.format("\\Shopware_Controllers_%s_%s", moduleName, controller);
        PhpClass phpClass = PhpElementsUtil.getClassInterface(smartyFile.getProject(), className);
        if(phpClass == null) {
            return null;
        }

        return PhpElementsUtil.getClassMethod(phpClass, action + "Action");

    }

    public static String toCamelCase(String value, boolean startWithLowerCase) {
        String[] strings = StringUtils.split(value.toLowerCase(), "_");
        for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++){
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return StringUtils.join(strings);
    }

    public static void collectControllerViewVariable(Method method, ControllerViewVariableVisitor controllerViewVariableVisitor) {

        // Views()->test;
        for(FieldReference fieldReference: PsiTreeUtil.collectElementsOfType(method, FieldReference.class)) {
            PsiElement methodReference = fieldReference.getFirstChild();
            if(methodReference instanceof MethodReference) {
                if(((MethodReference) methodReference).getCanonicalText().equals("View")) {

                    PsiElement parentElement = fieldReference.getParent();
                    if(parentElement instanceof AssignmentExpressionImpl) {
                        controllerViewVariableVisitor.visitVariable(fieldReference.getName(), methodReference, ((AssignmentExpressionImpl) parentElement).getValue());
                    } else {
                        // need this ???
                        //controllerViewVariableVisitor.visitVariable(fieldReference.getName(), null);
                    }

                }
            }
        }

        // Views()->assign('test'); // Views()->assign(['test' => 'test'])
        for(MethodReference methodReference: PsiTreeUtil.collectElementsOfType(method, MethodReference.class)) {

            if("assign".equals(methodReference.getName())) {
                PsiElement firstParameter = PsiElementUtils.getMethodParameterPsiElementAt(methodReference, 0);
                if(firstParameter instanceof ArrayCreationExpression) {
                    for(String keyName: PhpElementsUtil.getArrayCreationKeys((ArrayCreationExpression) firstParameter)) {
                        // @TODO: add source and type resolve
                        controllerViewVariableVisitor.visitVariable(keyName, firstParameter, null);
                    }
                } else {
                    String value = PhpElementsUtil.getStringValue(firstParameter);
                    if(value != null) {
                        // @TODO: add source and type resolve
                        controllerViewVariableVisitor.visitVariable(value, firstParameter, null);
                    }
                }
            }

        }
    }

    public interface ControllerViewVariableVisitor {
        public void visitVariable(String variableName, @NotNull PsiElement sourceType, @Nullable PsiElement typeElement);
    }

}