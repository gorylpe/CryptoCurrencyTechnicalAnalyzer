package pl.dimzi.cryptocurrencyanalyzer.bitbay.view;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PlotPanel extends JPanel{

    private ArrayList<CurrencyData> currencyData;
    private TradeType tradeType;
    private Period period;

    private long dateStart;
    private long dateEnd;

    private final Font dateFont = new Font("Arial", Font.ITALIC, 10);

    private final Color backgroundColor = Color.WHITE;
    private final Color helpLineColor = new Color(0, 0, 0, 64);
    private final Color averagesColor = new Color(48, 63, 159);

    private final Color volumeColor = new Color(66, 165, 255, 64);

    private final Color candleIncreaseColor = new Color(67, 160, 71);
    private final Color candleDecreaseColor = new Color(198, 40, 40);

    /**
     * Initializes default values of plot panel.
     */
    public PlotPanel() {
        super();

        currencyData = new ArrayList<>();
    }

    /**
     * Sets data used to plotting.
     * @param currencyData data array used to plotting.
     */
    public void refreshCurrencyData(TradeType tradeType, Period period, ArrayList<CurrencyData> currencyData) {
        Log.d(this, "Refreshing currency data");
        this.tradeType = tradeType;
        this.period = period;
        this.currencyData = currencyData;

        Log.d(this, "New dates, start " + dateStart + " end " + dateEnd + " elements " + this.currencyData.size());

        repaint();
    }

    public void setNewDateRange(long dateStart, long dateEnd){
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        repaint();
    }

    /**
     * Paints whole plot
     * @param g Graphics on which plot is drawn.
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if(currencyData != null)
            drawCandles(g2d);
    }

    private void drawCandles(Graphics2D g2d){
        g2d.setStroke(new BasicStroke(1.5f));

        List<CurrencyData> visible = new ArrayList<>();

        final long visibleDateStart = dateStart - period.getPeriodLength();
        final long visibleDateEnd = dateEnd + period.getPeriodLength();
        for(int i = 0; i < currencyData.size(); ++i){
            CurrencyData data = currencyData.get(i);
            if(data.getPeriodStart() > visibleDateStart && data.getPeriodStart() < visibleDateEnd){
                visible.add(data);
            }
        }

        if(visible.size() > 0){
            double valueMax = visible.stream().max(Comparator.comparingDouble(CurrencyData::getMaximum)).get().getMaximum();
            double valueMin = visible.stream().min(Comparator.comparingDouble(CurrencyData::getMinimum)).get().getMinimum();

            double xnum = (dateEnd - dateStart) / period.getPeriodLength();
            double dx = getWidth() / xnum;

            final int topBottomPadding = 10;
            double yscale = (getHeight() - 2*topBottomPadding) / (valueMax - valueMin);

            for(int i = 0; i < visible.size(); ++i){
                CurrencyData data = visible.get(i);

                //draw candle
                final boolean increase = data.getOpening() < data.getClosing();

                final int x = (int)Math.floor((1.0 * (data.getPeriodStart() - dateStart) / period.getPeriodLength() * dx));
                final int width = (int)dx;
                final int closingY = (int)((valueMax - data.getClosing()) * yscale) + topBottomPadding;
                final int openingY = (int)((valueMax - data.getOpening()) * yscale) + topBottomPadding;
                final int y = increase ? closingY : openingY;
                final int height = Math.max(Math.abs(closingY - openingY), 1);

                Color candleColor = increase ? candleIncreaseColor : candleDecreaseColor;

                g2d.setColor(candleColor);
                g2d.fillRect(x + width / 5, y, width * 3 / 5, height);

                final int minimumY = (int) ((valueMax - data.getMinimum()) * yscale) + topBottomPadding;
                final int maximumY = (int) ((valueMax - data.getMaximum()) * yscale) + topBottomPadding;
                //draw minmax
                g2d.setColor(candleColor);
                g2d.drawLine(x + width / 2 - 1, minimumY, x + width / 2 - 1, maximumY);
            }
        }
    }
}