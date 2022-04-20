package javaswingdev.chart;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
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
    private final int seriesSize = 10;
    private final int seriesSpace = 10;
    private Animator animator;
    private Animator animatorLabel;
    private TimingTarget targetLabel;
    private float animate;
    private String labelText;
    private int overIndex = -1;

    public BarChart() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill", "[fill]", "[fill]"));
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
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                for (int i = 0; i < legends.size(); i++) {
                    ModelLegend legend = legends.get(i);
                    double seriesValues = chart.getSeriesValuesOf(model.get(index).getValues()[i], size.getHeight());
                    int t = model.size() - index;
                    double seriesValuesAnimation = seriesValues * ease(animate, t);
                    GradientPaint gra = new GradientPaint(0, 0, new Color(253, 77, 77), 0, getHeight(), new Color(24, 14, 204));
                    RoundRectangle2D r2d = new RoundRectangle2D.Double(size.getX() + x, size.getY() + size.getHeight() - seriesValuesAnimation, seriesSize, seriesValuesAnimation, seriesSize, seriesSize);
                    g2.setPaint(gra);
                    g2.fill(r2d);
                    x += seriesSpace + seriesSize;
                }
                if (labelText != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    Dimension s = getLabelWidth(labelText, g2);
                    int space = 3;
                    int spaceTop = 5;
                    g2.setColor(new Color(150, 150, 150));
                    g2.fill(new RoundRectangle2D.Double(labelLocation.x - s.getWidth() / 2 - 3, labelLocation.y - s.getHeight() - space * 2 - spaceTop, s.getWidth() + space * 2, s.getHeight() + space * 2, 10, 10));
                    g2.setColor(new Color(248, 248, 248));
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    g2.drawString(labelText, labelLocation.x - s.width / 2, labelLocation.y - spaceTop - space * 2);
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

    private float ease(float f, int t) {
        double v = 1 - Math.pow(1 - f, t);
        return (float) v;
    }

    private int toIndex(int index, int i) {
        return index * legends.size() + i;
    }

    private void startAnimateLabel(Point oldLocation, Point newLocation) {
        animatorLabel.stop();
        animatorLabel.removeTarget(targetLabel);
        targetLabel = new PropertySetter(this, "changeLocation", oldLocation, newLocation);
        animatorLabel.addTarget(targetLabel);
        animatorLabel.start();
    }

    public void setChangeLocation(Point point) {
        labelLocation = point;
        repaint();
    }

    public void addLegend(String name, Color color) {
        ModelLegend data = new ModelLegend(name, color);
        legends.add(data);
        //  panelLegend.add(new LegendItem(data));
        //  panelLegend.repaint();
        //  panelLegend.revalidate();
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

    private Dimension getLabelWidth(String text, Graphics2D g2) {
        FontMetrics ft = g2.getFontMetrics();
        Rectangle2D r2 = ft.getStringBounds(text, g2);
        return new Dimension((int) r2.getWidth(), (int) r2.getHeight());
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        labelText = null;
        overIndex = -1;
    }

    private BlankPlotChart blankPlotChart;
}
