package javaswingdev.chart;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class LegendItem extends JLabel {

    private final ModelLegend legend;

    public LegendItem(ModelLegend legend) {
        this.legend = legend;
        setText(legend.getName());
        setBorder(new EmptyBorder(1, 25, 1, 1));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int height = getHeight();
        int size = 8;
        int sizeWidth = 20;
        int y = (height - size) / 2;
        g2.setPaint(new GradientPaint(0, 0, legend.getColor2(), sizeWidth, sizeWidth, legend.getColor1()));
        g2.fillRect(0, y, sizeWidth, size);
        g2.dispose();
        super.paintComponent(g);
    }
}
