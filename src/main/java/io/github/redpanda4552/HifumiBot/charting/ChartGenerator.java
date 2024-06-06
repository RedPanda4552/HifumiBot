package io.github.redpanda4552.HifumiBot.charting;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.util.Messaging;

public class ChartGenerator {

    public static byte[] buildWarezChart() {
        ArrayList<WarezChartData> warezDataList = Database.getWarezAssignmentsThisYear();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (WarezChartData data : warezDataList) {
            dataset.addValue(data.events, data.action, data.month);
        }

        JFreeChart chart = ChartFactory.createBarChart("Warez Events This Year", "Month", "Warez Events", dataset, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(true);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.GREEN);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(out, chart, 1280, 720);
            return out.toByteArray();
        } catch (Exception e) {
            Messaging.logException("ChartGenerator", "buildWarezChart", e);
        }
        
        return null;
    }

    public static byte[] buildMemberChart() {
        ArrayList<MemberChartData> memberDataList = Database.getMemberEventsThisYear();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (MemberChartData data : memberDataList) {
            dataset.addValue(data.events, data.action, data.month);
        }

        JFreeChart chart = ChartFactory.createBarChart("Member Events This Year", "Month", "Member Events", dataset, PlotOrientation.HORIZONTAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(true);
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesPaint(1, Color.YELLOW);
        renderer.setSeriesPaint(2, Color.RED);
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(out, chart, 1280, 720);
            return out.toByteArray();
        } catch (Exception e) {
            Messaging.logException("ChartGenerator", "buildMemberChart", e);
        }
        
        return null;
    }
}
