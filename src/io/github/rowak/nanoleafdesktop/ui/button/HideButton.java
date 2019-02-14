package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class HideButton extends JLabel
{
	public HideButton(final JFrame frame)
	{
		setText("–");
		setForeground(Color.WHITE);
		setFont(new Font("Tahoma", Font.BOLD, 30));
		addMouseListener(new MouseAdapter()
		{
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	if (e.getButton() == MouseEvent.BUTTON1)
            	{
            		final int TIME = 200;
            	    final int MILLIS_PER_FRAME = 33;
            	    final float DELTA = MILLIS_PER_FRAME / (float)TIME;
            		frame.setOpacity(1f);
                    final Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask()
                    {
                        float opacity = 1f;

                        @Override
                        public void run()
                        {
                            opacity += -DELTA;
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
            	}
            }
            
            @Override
			public void mouseEntered(MouseEvent e)
			{
            	((JLabel)e.getSource()).setForeground(Color.LIGHT_GRAY);
			}
            
            @Override
            public void mouseExited(MouseEvent e)
            {
            	((JLabel)e.getSource()).setForeground(Color.WHITE);
            }
        });
	}
}
