package io.github.redpanda4552.HifumiBot.charting;

public abstract class AbstractChartGenerator {

    public enum ChartType {
        WAREZ;
    }

    public AbstractChartGenerator() {
        
    }

    public abstract byte[] build();
}
