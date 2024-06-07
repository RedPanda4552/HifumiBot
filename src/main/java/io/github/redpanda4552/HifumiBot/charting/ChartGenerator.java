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

    public static byte[] buildWarezChart(String timeUnit, long length) {
        ArrayList<WarezChartData> warezDataList = null;
        
        if (timeUnit.equals("Day-of-Week")) {
            warezDataList = Database.getWarezAssignmentsByWeekday(timeUnit, length);
        } else {
            warezDataList = Database.getWarezAssignmentsSince(timeUnit, length);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (WarezChartData data : warezDataList) {
            dataset.addValue(data.events, data.action, data.timeUnit);
        }

        JFreeChart chart = ChartFactory.createBarChart("Warez Events (" + timeUnit + ", " + length + " days)", timeUnit, "Warez Events", dataset, PlotOrientation.VERTICAL, true, true, false);
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

    public static byte[] buildMemberChart(String timeUnit, long length) {
        ArrayList<MemberChartData> memberDataList = null;
        
        if (timeUnit.equals("Day-of-Week")) {
            memberDataList = Database.getMemberEventsByWeekday(timeUnit, length);
        } else {
            memberDataList = Database.getMemberEventsSince(timeUnit, length);
        }
        

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (MemberChartData data : memberDataList) {
            dataset.addValue(data.events, data.action, data.timeUnit);
        }

        JFreeChart chart = ChartFactory.createBarChart("Member Events (" + timeUnit + ", " + length + " days)", timeUnit, "Member Events", dataset, PlotOrientation.HORIZONTAL, true, true, false);
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
