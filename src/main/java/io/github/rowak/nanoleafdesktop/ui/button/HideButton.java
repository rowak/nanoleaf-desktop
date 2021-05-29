package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import io.github.rowak.nanoleafdesktop.Main;

public class HideButton extends JButton {
	
	public HideButton(final JFrame frame) {
//		setText((char)0x2013 + "");
		//setText("�");
		URL iconPath =
				Main.class.getResource("/images/minimize_button_icon.png");
		URL iconHighlightedPath =
				Main.class.getResource("/images/minimize_button_icon_highlighted.png");
		setIcon(new ImageIcon(iconPath));
		setRolloverIcon(new ImageIcon(iconHighlightedPath));
		setPressedIcon(new ImageIcon(iconHighlightedPath));
		setContentAreaFilled(false);
		setBorder(null);
		addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if (e.getButton() == MouseEvent.BUTTON1) {
            		final String os = System.getProperty("os.name").toLowerCase();
            		if (os.equals("linux")) {
            			frame.setState(JFrame.ICONIFIED);
            		}
            		else {
	            		final int TIME = 200;
	            	    final int MILLIS_PER_FRAME = 33;
	            	    final float DELTA = MILLIS_PER_FRAME / (float)TIME;
	            		frame.setOpacity(1f);
	                    final Timer timer = new Timer();
	                    TimerTask timerTask = new TimerTask() {
	                        float opacity = 1f;
	
	                        @Override
	                        public void run() {
	                            opacity += -DELTA;
	                            if (opacity < 0) {
	                                frame.setState(JFrame.ICONIFIED);
	                                frame.setOpacity(1f);
	                                timer.cancel();
	                            }
	                            else if (opacity > 1) {
	                                frame.setOpacity(1f);
	                                timer.cancel();
	                            }
	                            else {
	                                frame.setOpacity(opacity);
	                            }
	                        }
	                    };
	                    timer.scheduleAtFixedRate(timerTask, MILLIS_PER_FRAME, MILLIS_PER_FRAME);
            		}
            	}
            }
        });
	}
}
