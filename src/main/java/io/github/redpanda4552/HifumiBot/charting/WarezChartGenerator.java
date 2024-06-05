package io.github.redpanda4552.HifumiBot.charting;

import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import io.github.redpanda4552.HifumiBot.database.Database;
import io.github.redpanda4552.HifumiBot.util.Messaging;

public class WarezChartGenerator extends AbstractChartGenerator {

    public WarezChartGenerator() {
        
    }

    @Override
    public byte[] build() {
        ArrayList<WarezChartData> warezDataList = Database.getWarezAssignmentsMonth();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (WarezChartData data : warezDataList) {
            String monthName = Month.of(data.month).getDisplayName(TextStyle.SHORT, Locale.US);
            dataset.addValue(data.events, monthName, "events");
        }

        JFreeChart chart = ChartFactory.createBarChart("Warez Events This Year", "Month", "Warez Events", dataset, PlotOrientation.VERTICAL, true, true, false);
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(out, chart, 1280, 720);
            return out.toByteArray();
        } catch (Exception e) {
            Messaging.logException("WarezChartGenerator", "build", e);
        }
        
        return null;
    }

}
