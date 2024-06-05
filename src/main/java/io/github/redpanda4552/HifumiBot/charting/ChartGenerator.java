package io.github.redpanda4552.HifumiBot.charting;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.util.Messaging;

public class ChartGenerator {

    public static byte[] buildWarezChart() {
        ArrayList<WarezChartData> warezDataList = Database.getWarezAssignmentsThisYear();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (WarezChartData data : warezDataList) {
            dataset.addValue(data.events, data.month, "events");
        }

        JFreeChart chart = ChartFactory.createBarChart("Warez Events This Year", "Month", "Warez Events", dataset, PlotOrientation.VERTICAL, true, true, false);
        
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