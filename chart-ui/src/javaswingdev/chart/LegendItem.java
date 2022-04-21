package javaswingdev.chart;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class LegendItem extends JLabel {

    private final ModelLegend legend;

    public LegendItem(ModelLegend legend) {
        this.legend = legend;
        setText(legend.getName());
        setBorder(new EmptyBorder(1, 20, 1, 1));
    }
}
