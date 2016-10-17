package sirrandom.clock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

public class SystemClock extends JFrame
{
	
	private static final long serialVersionUID = 6917214343984233947L;
	
	private static final int						REVISION	= 4;
	private static final long						PREC		= 250;
	private static final Font						LC12		= new Font("Lucida Console", Font.PLAIN, 12);
	private static final	Font						LCB15		= new Font("Lucida Console", Font.BOLD,  15);
	private static final	Font						LCB22		= new Font("Lucida Console", Font.BOLD,  22);
	private static final	GridBagConstraints	GBC		= new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
	private static final Preferences				PREFS		= Preferences.userNodeForPackage(SystemClock.class);
	private static final String					PPX		= "sirrandom_clock-window_position_x";
	private static final String					PPY		= "sirrandom_clock-window_position_y";
	
	private JPanel			rootpane;
	private JPanel				windowbar;
	private JLabel					lbl_unixtime;
	private JLabel					lbl_specs;
	private JPanel				centerpanel;
	private GridBagLayout		gbl;
	private JPanel					centerpanelalign;
	private JLabel						lbl_curtime;
	private JSeparator				sep_vis;
	private JSeparator				sep_invis;
	private JLabel						lbl_curdate;
	private JPanel				statusbar;
	private JLabel					lbl_extdate;
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(() -> new SystemClock());
	}
	
	/** @wbp.parser.entryPoint */
	public SystemClock()
	{
		super("SYSTEM CLOCK REVISION " + REVISION + " / " + PREC + "MS PRECISION");
		setIconImage(Toolkit.getDefaultToolkit().getImage(SystemClock.class.getResource("/sirrandom/clock/thing2.png")));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setUndecorated(true);
		setAlwaysOnTop(true);
		
		setBounds(PREFS.getInt(PPX, 0), PREFS.getInt(PPY, 0), 160, 80);
		
		rootpane = new JPanel(new BorderLayout());
			rootpane.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new LineBorder(Color.WHITE)));
			rootpane.setBackground(Color.BLACK);
			rootpane.add(windowbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)), BorderLayout.NORTH);
				windowbar.setBorder(new MatteBorder(0, 0, 1, 0, Color.WHITE));
				windowbar.setBackground(Color.BLACK);
				windowbar.add(lbl_unixtime = new JLabel("UNIX TIME "), BorderLayout.CENTER);
					lbl_unixtime.setForeground(Color.CYAN);
					lbl_unixtime.setFont(LC12);
				windowbar.add(lbl_specs = new JLabel(REVISION + "/" + PREC));
					lbl_specs.setForeground(Color.MAGENTA);
					lbl_specs.setFont(LC12);
			rootpane.add(centerpanel = new JPanel(gbl = new GridBagLayout()));
				centerpanel.setBackground(Color.BLACK);
				gbl.rowWeights = gbl.columnWeights = new double[] {1.0};
				centerpanel.add(centerpanelalign = new JPanel(), GBC);
					centerpanelalign.setLayout(new BoxLayout(centerpanelalign, BoxLayout.Y_AXIS));
					centerpanelalign.setBackground(Color.BLACK);
					centerpanelalign.add(lbl_curtime = new JLabel("CUR TIME"));
						lbl_curtime.setAlignmentX(Component.CENTER_ALIGNMENT);
						lbl_curtime.setForeground(Color.YELLOW);
						lbl_curtime.setFont(LCB22);
					centerpanelalign.add(sep_vis = new JSeparator());
						sep_vis.setForeground(Color.YELLOW);
						sep_vis.setBackground(Color.BLACK);
					centerpanelalign.add(sep_invis = new JSeparator());
						sep_invis.setForeground(Color.BLACK);
						sep_invis.setBackground(Color.BLACK);
					centerpanelalign.add(lbl_curdate = new JLabel("CUR DATE"));
						lbl_curdate.setAlignmentX(Component.CENTER_ALIGNMENT);
						lbl_curdate.setForeground(Color.YELLOW);
						lbl_curdate.setFont(LCB15);
			rootpane.add(statusbar = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
				statusbar.setBorder(new MatteBorder(1, 0, 0, 0, Color.WHITE));
				statusbar.setBackground(Color.BLACK);
				statusbar.add(lbl_extdate = new JLabel("EXTENDED TIME & DATE"), BorderLayout.CENTER);
					lbl_extdate.setHorizontalAlignment(SwingConstants.CENTER);
					lbl_extdate.setForeground(Color.CYAN);
					lbl_extdate.setFont(LC12);
		
		new Handler(this, lbl_unixtime, lbl_curtime, lbl_curdate, lbl_extdate);
		
		setContentPane(rootpane);
		setVisible(true);
	}
	
	public static class Handler implements MouseListener, KeyListener, MouseMotionListener, Runnable
	{
		
		private static SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
		private static SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/YYYY");
		private static SimpleDateFormat sdf3 = new SimpleDateFormat("EEE MMMM dd z");
		private static Date date;
		
		private boolean	loop = true;
		
		private Point		dragpointa;
		private Point		dragpointb;
		private JLabel		a, b, c, d;
		private JFrame		frame;
		
		public Handler(JFrame frame, JLabel a, JLabel b, JLabel c, JLabel d)
		{
			this.frame	= frame;
			
			this.a		= a;
			this.b		= b;
			this.c		= c;
			this.d		= d;
			
			frame.addKeyListener(this);
			frame.addMouseListener(this);
			frame.addMouseMotionListener(this);
			
			new Thread(this).start();
		}
		
		@Override
		public void run()
		{
			while(loop)
			{
				date = new Date();
				
				a.setText((System.currentTimeMillis() / 1000L) + " ");
				b.setText(sdf1.format(date).toUpperCase());
				c.setText(sdf2.format(date).toUpperCase());
				d.setText(sdf3.format(date).toUpperCase());
				
				try { Thread.sleep(PREC); } catch(InterruptedException e) {}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			dragpointa = e.getPoint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			frame.setLocation((dragpointb = frame.getLocation()).x + e.getX() - dragpointa.x, dragpointb.y + e.getY() - dragpointa.y);
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			PREFS.putInt(PPX, frame.getX());
			PREFS.putInt(PPY, frame.getY());
		}
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_F4 && e.isAltDown())
			{
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				loop = false;
				e.consume();
			}
		}
		
		@Override public void mouseClicked(MouseEvent e)	{}
		@Override public void mouseEntered(MouseEvent e)	{}
		@Override public void mouseExited(MouseEvent e)		{}
		@Override public void mouseMoved(MouseEvent e)		{}
		@Override public void keyReleased(KeyEvent e)		{}
		@Override public void keyTyped(KeyEvent e)			{}
		
	}
	
}
