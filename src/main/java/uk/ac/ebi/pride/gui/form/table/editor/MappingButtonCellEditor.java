package uk.ac.ebi.pride.gui.form.table.editor;

import uk.ac.ebi.pride.App;
import uk.ac.ebi.pride.data.model.DataFile;
//import uk.ac.ebi.pride.gui.form.dialog.FileMappingDialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MappingButtonCellEditor extends ButtonCellEditor {
    private Object value;

    public MappingButtonCellEditor(String text, Icon icon) {
        super(text, icon);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.value = value;
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    @Override
    public Object getCellEditorValue() {
//        if (isPushed) {
//            FileMappingDialog dialog = new FileMappingDialog(((App)App.getInstance()).getMainFrame(), (DataFile)value);
//            dialog.setLocationRelativeTo(((App)App.getInstance()).getMainFrame());
//            dialog.setVisible(true);
//        }
        return super.getCellEditorValue();
    }
}
