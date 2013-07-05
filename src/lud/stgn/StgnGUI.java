package lud.stgn;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.JApplet;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class StgnGUI
	extends
	JApplet {

	private JFrame frame;
	private JTextField txtSize;
	private JTextField txtMask;
	private JButton btnOpenContainerImgFile;
	private JButton btnSecret;
	private JButton btnConceal;
	private JButton btnUnveil;

	/**
	 * @wbp.nonvisual location=134,9
	 */
	private final JFileChooser fileChsrContainer = new JFileChooser( "./" );
	/**
	 * @wbp.nonvisual location=254,9
	 */
	private final JFileChooser fileChsrSecret = new JFileChooser( "./" );
	/**
	 * @wbp.nonvisual location=204,309
	 */
	private final JFileChooser fileChsrSaveAs = new JFileChooser( "./" );

	private BufferedImage containerImg;
	private File secretFile;
	private int mask = Stgn.DEFAULT_MASK;
	private static final long serialVersionUID = 1L;
	private int secretSize;

	/**
	 * Launch the applet.
	 */
	public void init() {

		main( null );
	}

	/**
	 * Launch the application.
	 */
	public static void main( String[] args ) {

		EventQueue.invokeLater( new Runnable() {

			public void run() {

				try {
					StgnGUI window = new StgnGUI();
					window.frame.setVisible( true );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		} );
	}

	/**
	 * Create the application.
	 */
	public StgnGUI() {

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */

	private void initialize() {

		frame = new JFrame();
		frame.setBounds( 100, 100, 480, 320 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().setLayout( null );

		btnOpenContainerImgFile = new JButton( "Container Image" );
		btnOpenContainerImgFile.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e ) {

				try {
					if ( JFileChooser.APPROVE_OPTION == fileChsrContainer
						.showOpenDialog( frame ) ) {
						File imgFile = fileChsrContainer.getSelectedFile();
						containerImg =
							ImageIO.read( Util.validImageFile( imgFile ) );
						Image btnImg =
							containerImg.getScaledInstance(
								btnOpenContainerImgFile.getWidth(),
								btnOpenContainerImgFile.getHeight(),
								Image.SCALE_SMOOTH );
						btnOpenContainerImgFile
							.setIcon( new ImageIcon( btnImg ) );
						btnOpenContainerImgFile.setText( imgFile.getName() );
						btnOpenContainerImgFile.setToolTipText( imgFile
							.getAbsoluteFile()
							.toString() );
						btnConceal.setEnabled( true );
						btnUnveil.setEnabled( true );
						btnSecret.setEnabled( true );
					}
				} catch ( Exception ex ) {
					JOptionPane.showMessageDialog( frame, ex.getMessage(), ex
						.getClass()
						.toString(), JOptionPane.WARNING_MESSAGE );
				}
			}
		} );
		btnOpenContainerImgFile.setBounds( 10, 59, 163, 164 );
		frame.getContentPane().add( btnOpenContainerImgFile );

		btnSecret = new JButton( "Secret" );
		btnSecret.setEnabled( false );
		btnSecret.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e ) {

				try {
					if ( JFileChooser.APPROVE_OPTION == fileChsrSecret
						.showOpenDialog( frame ) ) {
						secretFile = fileChsrSecret.getSelectedFile();
						btnSecret.setText( secretFile.getName() );
						btnSecret.setToolTipText( secretFile
							.getAbsoluteFile()
							.toString() );
						btnConceal.setEnabled( true );
					}
				} catch ( Exception ex ) {
					JOptionPane.showMessageDialog( frame, ex.getMessage(), ex
						.getClass()
						.toString(), JOptionPane.WARNING_MESSAGE );
				}
			}
		} );
		btnSecret.setBounds( 286, 59, 156, 164 );
		frame.getContentPane().add( btnSecret );

		btnConceal = new JButton( "conceal" );
		btnConceal.setEnabled( false );
		btnConceal.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e ) {

				try {
					if ( null == secretFile ) {
						btnConceal.setEnabled( false );
						throw new Exception( "choose secretFile to conceal" );
					}
					if ( null == containerImg ) {
						btnConceal.setEnabled( false );
						throw new Exception( "choose Container Image" );
					}

					Stgn stgn1 = new Stgn( containerImg, mask );
					BufferedImage resultImg = stgn1.conceal( secretFile );
					fileChsrSaveAs.setSelectedFile( new File(
						"ihaveasecret_mask("
							+ "0x"
							+ String.format( "%08X", mask )
							+ ")_size("
							+ ( Long.toString( secretFile.length() ) )
							+ ").png" ) );
					if ( JFileChooser.APPROVE_OPTION == fileChsrSaveAs
						.showSaveDialog( frame ) ) {
						File saveAsFile = fileChsrSaveAs.getSelectedFile();
						ImageIO.write( resultImg, "png", saveAsFile );
					}

				} catch ( Exception ex ) {
					JOptionPane.showMessageDialog( frame, ex.getMessage(), ex
						.getClass()
						.toString(), JOptionPane.WARNING_MESSAGE );
				}

			}
		} );
		btnConceal.setBounds( 187, 75, 89, 23 );
		frame.getContentPane().add( btnConceal );

		btnUnveil = new JButton( "unveil" );
		btnUnveil.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e ) {

				try {
					if ( null == containerImg ) {
						btnUnveil.setEnabled( false );
						throw new Exception( "choose Container Image" );
					}

					if ( 0 == secretSize ) {
						btnUnveil.setEnabled( false );
						txtSize.setText( "bytes to unveil.." );
						return;
					}
					Stgn stgn2 = new Stgn( containerImg, mask );
					ByteArrayOutputStream bOutS =
						( ByteArrayOutputStream ) stgn2.unveil( secretSize );

					fileChsrSaveAs.setSelectedFile( new File( "unveiled("
						+ "0x"
						+ String.format( "%08X", mask )
						+ ")_size("
						+ ( Long.toString( secretSize ) )
						+ ").dat" ) );
					if ( JFileChooser.APPROVE_OPTION == fileChsrSaveAs
						.showSaveDialog( frame ) ) {
						File saveAsFile = fileChsrSaveAs.getSelectedFile();
						OutputStream revealed =
							new FileOutputStream( saveAsFile );
						revealed.write( bOutS.toByteArray(), 0, secretSize );
						revealed.close();
					}

				} catch ( Exception ex ) {
					JOptionPane.showMessageDialog( frame, ex.getMessage(), ex
						.getClass()
						.toString(), JOptionPane.WARNING_MESSAGE );
				}
			}
		} );
		btnUnveil.setEnabled( false );
		btnUnveil.setBounds( 187, 181, 89, 23 );
		frame.getContentPane().add( btnUnveil );

		txtSize = new JTextField( "bytes to unveil.." );
		txtSize.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e ) {

				try {
					secretSize = Integer.parseInt( txtSize.getText() );
					btnUnveil.setEnabled( true );
				} catch ( Exception ex ) {
					txtSize.setText( "bytes to unveil.." );
					btnUnveil.setEnabled( false );
				}
			}
		} );
		txtSize.addFocusListener( new FocusAdapter() {

			@Override
			public void focusLost( FocusEvent e ) {

				try {
					secretSize = Integer.parseInt( txtSize.getText() );
					btnUnveil.setEnabled( true );
				} catch ( Exception ex ) {
					txtSize.setText( "bytes to unveil.." );
					btnUnveil.setEnabled( false );
				}
			}
		} );
		txtSize.setText( "bytes to unveil.." );
		txtSize.setBounds( 187, 150, 89, 20 );
		frame.getContentPane().add( txtSize );
		txtSize.setColumns( 10 );

		txtMask =
			new JTextField( "0x" + String.format( "%08X", Stgn.DEFAULT_MASK ) );
		txtMask.addFocusListener( new FocusAdapter() {

			@Override
			public void focusLost( FocusEvent arg0 ) {

				try {
					mask = Util.validMask( txtMask.getText() );
					System.out.println( mask );
				} catch ( Exception ex ) {
					System.err.println( ex.getMessage() );
					txtMask.setText( "0x" + String.format( "%08X", mask ) );
				}
			}
		} );
		txtMask.setToolTipText( "0xAARRGGBB" );
		txtMask.addActionListener( new ActionListener() {

			public void actionPerformed( ActionEvent e ) {

				try {
					mask = Util.validMask( e.getActionCommand() );
					System.err.println( mask );
				} catch ( Exception ex ) {
					txtMask.setText( "0x" + String.format( "%08X", mask ) );
					JOptionPane.showMessageDialog( frame, ex.getMessage(), ex
						.getClass()
						.toString(), JOptionPane.WARNING_MESSAGE );
				}
			}
		} );
		txtMask.setText( "0x03030303" );
		txtMask.setBounds( 187, 108, 89, 20 );
		frame.getContentPane().add( txtMask );
		txtMask.setColumns( 10 );
	}
}
