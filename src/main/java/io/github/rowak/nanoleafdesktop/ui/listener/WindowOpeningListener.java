package io.github.rowak.nanoleafdesktop.ui.listener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class WindowOpeningListener extends WindowAdapter
{
	JFrame frame;
	
	public WindowOpeningListener(JFrame frame)
	{
		this.frame = frame;
	}
	
	@Override
	public void windowDeiconified(WindowEvent e)
	{
		final int TIME = 200;
	    final int MILLIS_PER_FRAME = 33;
	    final float DELTA = MILLIS_PER_FRAME / (float)TIME;
		frame.setOpacity(0f);
        frame.setState(JFrame.NORMAL); 
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            float opacity = 0f;

            @Override
            public void run()
            {
                opacity += DELTA;
                
                if (opacity < 0)
                {
                    frame.setState(JFrame.ICONIFIED);
                    frame.setOpacity(1f);
                    timer.cancel();
                }
                else if (opacity > 1)
                {
                    frame.setOpacity(1f);
                    timer.cancel();
                }
                else
                {
                    frame.setOpacity(opacity);
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, MILLIS_PER_FRAME, MILLIS_PER_FRAME);
		super.windowDeiconified(e);
	}
}
