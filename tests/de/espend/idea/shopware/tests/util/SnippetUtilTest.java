package de.espend.idea.shopware.tests.util;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.smarty.SmartyFile;
import de.espend.idea.shopware.tests.ShopwareLightCodeInsightFixtureTestCase;
import de.espend.idea.shopware.util.SnippetUtil;
import de.espend.idea.shopware.util.dict.ShopwareSnippet;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SnippetUtilTest extends ShopwareLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testGetSnippetsInFile() {
        PsiFile psiFile = myFixture.configureByFile("snippets.tpl");

        Collection<ShopwareSnippet> snippets = SnippetUtil.getSnippetsInFile((SmartyFile) psiFile);

        assertNotNull(ContainerUtil.find(snippets, snippet ->
            "DetailLinkNotepad".equals(snippet.getName()) && "frontend/detail/actions".equals(snippet.getNamespace()))
        );

        assertNotNull(ContainerUtil.find(snippets, snippet ->
            "FO-O/BAR".equals(snippet.getName()) && "frontend/foobar".equals(snippet.getNamespace()))
        );
    }

    public void testGetFileNamespace() {
        PsiFile psiFile = myFixture.configureByFile("namespace.tpl");
        assertEquals("frontend/listing/box_article", SnippetUtil.getFileNamespace((SmartyFile) psiFile));
    }

    public void testGetIniKeys() {
        PsiFile psiFile = myFixture.configureByFile("widgets.ini");
        Set<String> iniKeys = SnippetUtil.getIniKeys(psiFile.getText());

        assertContainsElements(
            iniKeys,
            "swag-last-registrations/customer-group", "swag-last-registrations/customer", "swag-last-regist_?0drations/date"
        );
    }

    public void testGetSnippetKeysByNamespace() {
        myFixture.configureByFile("snippets.tpl");

        assertContainsElements(
            SnippetUtil.getSnippetKeysByNamespace(getProject(),"frontend/detail/actions"),
            "DetailLinkNotepad"
        );
    }

    public void testGetSnippetNamespaces() {
        myFixture.configureByFile("snippets.tpl");

        assertContainsElements(
            SnippetUtil.getSnippetNamespaces(getProject()),
            "frontend/detail/actions"
        );
    }

    public void testGetSnippetNamespaceTargets() {
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");

        assertNotNull(ContainerUtil.find(SnippetUtil.getSnippetNamespaceTargets(getProject(), "foobar/widgets"), virtualFile ->
            "ini".equalsIgnoreCase(virtualFile.getContainingFile().getVirtualFile().getExtension()))
        );
    }

    public void testGetSnippetNameTargets() {
        myFixture.copyFileToProject("widgets.ini", "snippets/foobar/widgets.ini");

        Collection<PsiElement> snippets = SnippetUtil.getSnippetNameTargets(
            getProject(),
            "foobar/widgets",
            "swag-last-registrations/customer"
        );

        assertNotNull(ContainerUtil.find(snippets, psiElement ->
            "widgets.ini".equals(psiElement.getContainingFile().getVirtualFile().getName())
        ));
    }

    public void testSnippetsForBackend() {
        PsiFile psiFile = myFixture.configureByFile("snippets.js");

        Collection<ShopwareSnippet> snippetsInFile = SnippetUtil.getSnippetsInFile((JSFile) psiFile);

        assertNotNull(ContainerUtil.find(snippetsInFile, snippet ->
            "backend/foobar/namespace".equals(snippet.getNamespace()) && "start_accept".equals(snippet.getName())
        ));

        assertNotNull(ContainerUtil.find(snippetsInFile, snippet ->
            "backend/foobar".equals(snippet.getNamespace()) && "start_accept".equals(snippet.getName())
        ));

        assertNotNull(ContainerUtil.find(snippetsInFile, snippet ->
            "backend/foobar/namespace".equals(snippet.getNamespace()) && "filter_feature".equals(snippet.getName())
        ));

        assertNotNull(ContainerUtil.find(snippetsInFile, snippet ->
            "foobar".equals(snippet.getNamespace()) && "filter_feature".equals(snippet.getName())
        ));
    }
}
