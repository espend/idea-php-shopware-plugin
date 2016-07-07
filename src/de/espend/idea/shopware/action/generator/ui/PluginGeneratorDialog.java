package de.espend.idea.shopware.action.generator.ui;

import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.action.generator.dict.PluginGeneratorSettings;
import fr.adrienbrault.idea.symfony2plugin.Symfony2Icons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PluginGeneratorDialog extends JDialog {
    @NotNull
    private final Callback callback;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textPluginName;
    private JCheckBox filterCheckBox;
    private JComboBox namespaceComboBox;
    private JCheckBox addDummyFrontendControllerCheckBox;
    private JCheckBox addDummyBackendControllerCheckBox;
    private JCheckBox addDummyModelsCheckBox;
    private JCheckBox addDummyCommandCheckBox;
    private JCheckBox addDummyWidgetCheckBox;
    private JCheckBox addDummyApiCheckBox;

    public PluginGeneratorDialog(@NotNull Callback callback) {
        this.callback = callback;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.namespaceComboBox.addItem("Frontend");
        this.namespaceComboBox.addItem("Core");
        this.namespaceComboBox.addItem("Backend");

        this.namespaceComboBox.setSelectedIndex(0);
    }

    private void onOK() {
        if (StringUtils.isBlank(textPluginName.getText())) {
            return;
        }

        this.callback.onOk(new PluginGeneratorSettings(
                textPluginName.getText(),
                (String) namespaceComboBox.getSelectedItem(),
                filterCheckBox.isSelected(),
                addDummyFrontendControllerCheckBox.isSelected(),
                addDummyBackendControllerCheckBox.isSelected(),
                addDummyModelsCheckBox.isSelected(),
                addDummyCommandCheckBox.isSelected(),
                addDummyWidgetCheckBox.isSelected(),
                addDummyApiCheckBox.isSelected()
        ));

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static PluginGeneratorDialog createAndShow(@NotNull Callback callback) {
        PluginGeneratorDialog dialog = new PluginGeneratorDialog(callback);
        dialog.setTitle("Shopware: Plugin Generator");
        dialog.setIconImage(Symfony2Icons.getImage(ShopwarePluginIcons.SHOPWARE));
        dialog.pack();

        dialog.setMinimumSize(new Dimension(350, 250));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return dialog;
    }

    public interface Callback {
        void onOk(@NotNull PluginGeneratorSettings settings);
    }
}