package javaswingdev.chart;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javaswingdev.chart.blankchart.BlankPlotChart;
import javaswingdev.chart.blankchart.BlankPlotChatRender;
import javaswingdev.chart.blankchart.SeriesSize;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

public class BarChart extends JComponent {

    private final DecimalFormat df = new DecimalFormat("#,##0.##");
    private final List<ModelLegend> legends = new ArrayList<>();
    private final List<ModelChart> model = new ArrayList<>();
    private Point labelLocation = new Point();
    private final int seriesSize = 8;
    private final int seriesSpace = 10;
    private Animator animator;
    private Animator animatorLabel;
    private TimingTarget targetLabel;
    private float animate;
    private String labelText;
    private int overIndex = -1;
    private Color displayTextColor = new Color(200, 200, 200);
    private Color displayTextBackground = new Color(70, 70, 70);

    public BarChart() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill, inset 0", "[fill]", "[]10[fill,100%]5"));
        setForeground(new Color(120, 120, 120));
        createPanelLegend();
        createBlankChart();
        createChart();
        createAnimatorChart();
        createAnimatorLabel();
    }

    private void createAnimatorChart() {
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                animate = fraction;
                repaint();
            }
        };
        animator = new Animator(1000, target);
        animator.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
    }

    private void createAnimatorLabel() {
        animatorLabel = new Animator(350);
        animatorLabel.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
    }

    private void createBlankChart() {
        blankPlotChart = new BlankPlotChart();
        add(blankPlotChart);
    }

    private void createChart() {
        blankPlotChart.setBlankPlotChatRender(new BlankPlotChatRender() {
            @Override
            public int getMaxLegend() {
                return legends.size();
            }

            @Override
            public String getLabelText(int index) {
                return model.get(index).getLabel();
            }

            @Override
            public void renderSeries(BlankPlotChart chart, Graphics2D g2, SeriesSize size, int index) {
                double totalSeriesWidth = (seriesSize * legends.size()) + (seriesSpace * (legends.size() - 1));
                double x = (size.getWidth() - totalSeriesWidth) / 2;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                for (int i = 0; i < legends.size(); i++) {
                    ModelLegend legend = legends.get(i);
                    double seriesValues = chart.getSeriesValuesOf(model.get(index).getValues()[i], size.getHeight());
                    int t = model.size() - index;
                    double seriesValuesAnimation = seriesValues * ease(animate, t);
                    GradientPaint gra = new GradientPaint(0, 0, legend.getColor1(), 0, getHeight(), legend.getColor2());
                    RoundRectangle2D r2d = new RoundRectangle2D.Double(size.getX() + x, size.getY() + size.getHeight() - seriesValuesAnimation, seriesSize, seriesValuesAnimation, seriesSize, seriesSize);
                    g2.setPaint(gra);
                    g2.fill(r2d);
                    x += seriesSpace + seriesSize;
                }
                if (labelText != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    Rectangle2D s = getLabelWidth(labelText, g2);
                    float space = 3;
                    float spaceTop = 5;
                    g2.setColor(getDisplayTextBackground());
                    g2.fill(new RoundRectangle2D.Double(labelLocation.getX() - s.getWidth() / 2 - 3, labelLocation.getY() - s.getHeight() - space * 2 - spaceTop, s.getWidth() + space * 2, s.getHeight() + space * 2, 10, 10));
                    g2.setColor(displayTextColor);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    float sx = (float) (labelLocation.getX() - s.getWidth() / 2);
                    float sy = (float) (labelLocation.getY() - spaceTop - space * 2);
                    g2.drawString(labelText, sx, sy);
                }
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            @Override
            public void renderSeries(BlankPlotChart chart, Graphics2D g2, SeriesSize size, int index, List<Path2D.Double> gra) {
            }

            @Override
            public void renderGraphics(Graphics2D g2, List<Path2D.Double> gra) {

            }

            @Override
            public boolean mouseMoving(BlankPlotChart chart, MouseEvent evt, Graphics2D g2, SeriesSize size, int index) {
                double totalSeriesWidth = (seriesSize * legends.size()) + (seriesSpace * (legends.size() - 1));
                double x = (size.getWidth() - totalSeriesWidth) / 2;
                int s = seriesSize / 2;
                for (int i = 0; i < legends.size(); i++) {
                    double seriesValues = chart.getSeriesValuesOf(model.get(index).getValues()[i], size.getHeight());
                    int t = model.size() - index;
                    double seriesValuesAnimation = seriesValues * ease(animate, t);
                    RoundRectangle2D r2d = new RoundRectangle2D.Double(size.getX() + x, size.getY() + size.getHeight() - seriesValuesAnimation, seriesSize, seriesValuesAnimation, seriesSize, seriesSize);
                    if (r2d.contains(evt.getPoint())) {
                        double data = model.get(index).getValues()[i];
                        labelText = df.format(data);
                        int seriesIndex = toIndex(index, i);
                        if (overIndex != seriesIndex) {
                            Point newPoint = new Point((int) (size.getX() + x + s), (int) (size.getY() + size.getHeight() - seriesValues));
                            Point oldPoint = overIndex != -1 ? labelLocation : new Point(newPoint.x, newPoint.y + 10);
                            startAnimateLabel(oldPoint, newPoint);
                            overIndex = seriesIndex;
                        }
                        return true;
                    }
                    x += seriesSpace + seriesSize;
                }
                return false;
            }
        });
    }

    private void createPanelLegend() {
        panelLegend = new JPanel();
        panelLegend.setOpaque(false);
        panelLegend.setLayout(new MigLayout("filly, center, inset 0", "[]10[]"));
        labelTitle = new JLabel();
        labelTitle.setForeground(new Color(229, 229, 229));
        labelTitle.setFont(labelTitle.getFont().deriveFont(Font.BOLD, 15));
        panelLegend.add(labelTitle, "push, gap left 10");
        add(panelLegend, "wrap");
    }

    private float ease(float f, int t) {
        double v = 1 - Math.pow(1 - f, t);
        return (float) v;
    }

    private int toIndex(int index, int i) {
        return index * legends.size() + i;
    }

    private void startAnimateLabel(Point oldLocation, Point newLocation) {
        if (animatorLabel.isRunning()) {
            animatorLabel.stop();
        }
        animatorLabel.removeTarget(targetLabel);
        targetLabel = new PropertySetter(this, "changeLocation", oldLocation, newLocation);
        animatorLabel.addTarget(targetLabel);
        animatorLabel.start();
    }

    public void setChangeLocation(Point point) {
        labelLocation = point;
        repaint();
    }

    public void addLegend(String name, Color color1, Color color2) {
        ModelLegend data = new ModelLegend(name, color1, color2);
        legends.add(data);
        LegendItem legend = new LegendItem(data);
        legend.setForeground(getForeground());
        panelLegend.add(legend);
        panelLegend.repaint();
        panelLegend.revalidate();
    }

    public void addData(ModelChart data) {
        model.add(data);
        blankPlotChart.setLabelCount(model.size());
        double max = data.getMaxValues();
        if (max > blankPlotChart.getMaxValues()) {
            blankPlotChart.setMaxValues(max);
        }
    }

    public void clear() {
        animate = 0;
        labelText = null;
        blankPlotChart.setLabelCount(0);
        model.clear();
        repaint();
    }

    public void start() {
        labelText = null;
        overIndex = -1;
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    public void resetAnimation() {
        labelText = null;
        overIndex = -1;
        animate = 0;
        repaint();
    }

    public void setTitle(String title) {
        labelTitle.setText(title);
    }

    public String getTitle() {
        return labelTitle.getText();
    }

    public void setTitleFont(Font font) {
        labelTitle.setFont(font);
    }

    public Font getTitleFont() {
        return labelTitle.getFont();
    }

    public void setTitleColor(Color color) {
        labelTitle.setForeground(color);
    }

    public Color getTitleColor() {
        return labelTitle.getForeground();
    }

    public Color getDisplayTextColor() {
        return displayTextColor;
    }

    public void setDisplayTextColor(Color displayTextColor) {
        this.displayTextColor = displayTextColor;
    }

    public Color getDisplayTextBackground() {
        return displayTextBackground;
    }

    public void setDisplayTextBackground(Color displayTextBackground) {
        this.displayTextBackground = displayTextBackground;
    }

    private Rectangle2D getLabelWidth(String text, Graphics2D g2) {
        FontMetrics ft = g2.getFontMetrics();
        Rectangle2D r2 = ft.getStringBounds(text, g2);
        return r2;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        labelText = null;
        overIndex = -1;
    }

    private BlankPlotChart blankPlotChart;
    private JPanel panelLegend;
    private JLabel labelTitle;
}
