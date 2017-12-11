package com.example.user.heartbeatapp;

import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

class LineChartView {

    private static Context mContext;
    /**
     *
     * @param chartTitle GraphTitle
     * @param XYSeries meaning of Series
     * @param list_Date X Data
     * @param list_Count Y Data
     * @return
     */
    public static View ChartView(Context context, String chartTitle,
                                 String XYSeries, List<String> list_Date, List<Integer> list_Count) {

        LineChartView.mContext = context;

        // 資料來源及命名
        XYSeries xySeries = new XYSeries(XYSeries);
        for(int i=0;i<list_Count.size();i++){
            xySeries.add(i+1, list_Count.get(i));
        }

        // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        // Adding Income Series to the dataset
        dataset.addSeries(xySeries);

        // 線的描述
        XYSeriesRenderer xySeriesRenderer = new XYSeriesRenderer();
        xySeriesRenderer.setColor(Color.RED);
        xySeriesRenderer.setChartValuesTextSize(40);// Value Text Size
        xySeriesRenderer.setPointStyle(PointStyle.CIRCLE);
        xySeriesRenderer.setFillPoints(true);
        xySeriesRenderer.setLineWidth(5);
        xySeriesRenderer.setDisplayChartValues(true);

        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setXLabels(0);
        multiRenderer.setChartTitle(chartTitle);
        multiRenderer.setChartTitleTextSize(40);
        multiRenderer.setXTitle("Date");// X Title
        multiRenderer.setYTitle("Count");// Y Title
        multiRenderer.setLabelsTextSize(40);// Label Text Size
        multiRenderer.setAxisTitleTextSize(40);// Axis Title Text Size
        multiRenderer.setZoomButtonsVisible(true);// Zoom?
        multiRenderer.setShowGrid(true);// show Grid
        for(int i=0; i<list_Date.size(); i++){
            multiRenderer.addXTextLabel(i+1, "" + list_Date.get(i).toString());
        }

        multiRenderer.addSeriesRenderer(xySeriesRenderer);

        View mChart = ChartFactory.getLineChartView(mContext, dataset, multiRenderer);

        return mChart;

    }

}