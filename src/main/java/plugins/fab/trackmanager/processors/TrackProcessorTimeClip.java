package plugins.fab.trackmanager.processors;


import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerListener;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.sequence.SequenceListener;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import plugins.fab.trackmanager.PluginTrackManagerProcessor;
import plugins.fab.trackmanager.TrackSegment;
import plugins.nchenouard.spot.Detection;

/**
 * 
 * @author Fabrice de Chaumont
 *
 * WARNING: BETA Version
 */
public class TrackProcessorTimeClip  extends PluginTrackManagerProcessor implements ActionListener , SequenceListener, ViewerListener {
	
	JTextField clipTextField = new JTextField("  3");
	JCheckBox futureCheckBox = new JCheckBox( "Display future.",false );
	JCheckBox nonActiveTracksCheckBox  = new JCheckBox("Display non active tracks", true );
	
	public TrackProcessorTimeClip() {
		
		setName( "Track Clipper" );
//		super(trackPool , "Track Clipper");
//		setPanelHeight( 90 );
		
		panel.setLayout(new GridLayout(3,1));
		JPanel clipPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		clipPanel.add( new JLabel("Number of detection to show before & after current Detection") );
		clipPanel.add( clipTextField );
		panel.add(clipPanel);
		
		panel.add( futureCheckBox );
		futureCheckBox.addActionListener( this );
		
		panel.add(nonActiveTracksCheckBox);
		nonActiveTracksCheckBox.addActionListener(this);
		
		//trackPool.getSequence().addSequenceChangeListener( this );
	}

	@Override
	public void Compute() {
		
		Sequence displaySequence = trackPool.getDisplaySequence();
		if ( displaySequence != null )
		{
			displaySequence.removeListener( this );
			displaySequence.addListener( this );

			Viewer viewer = displaySequence.getFirstViewer();
			if ( viewer != null )
			{
				viewer.removeListener( this );
				viewer.addListener( this );
			}
		}
		
		if ( isEnabled() )
		{
			double clipValue = 3;
			try
			{
				clipValue = new Double( clipTextField.getText() );
			}
			catch( Exception e )
			{
				clipValue = 3;
			}
			
			//int t = trackPool.getDisplaySequence().getSelectedT();
			// Find selected T. TODO: manage multiple viewers. Work if only one viewer is active.
			ArrayList<Viewer> viewerList = Icy.getMainInterface().getViewers( trackPool.getDisplaySequence() );
			int t=0;
			try
			{
				t = viewerList.get( 0 ).getPositionT();
				
			}
			catch( IndexOutOfBoundsException e)
			{
				t=0;
			}
			catch( NullPointerException e)
			{
				t=0;
			}
			
			if (nonActiveTracksCheckBox.isSelected())
				for ( Detection d : trackPool.getAllDetection() )
				{
					if ( d.getT() < t - clipValue ) d.setEnabled( false );				
					if ( d.getT() > t + clipValue ) d.setEnabled( false );
					if ( !futureCheckBox.isSelected() )
						if ( d.getT() > t ) d.setEnabled( false );
				}
			else
				for (TrackSegment ts:trackPool.getTrackSegmentList())
				{
					if (ts.getFirstDetection().getT()<=t && ts.getLastDetection().getT()>=t)
						for (Detection d:ts.getDetectionList())
						{
							if ( d.getT() < t - clipValue ) d.setEnabled( false );				
							if ( d.getT() > t + clipValue ) d.setEnabled( false );
							if ( !futureCheckBox.isSelected() )
								if ( d.getT() > t ) d.setEnabled( false );
						}
					else 
						ts.setAllDetectionEnabled(false);
				}
		}
		
	}

	public void actionPerformed(ActionEvent e) {
		trackPool.fireTrackEditorProcessorChange();		
	}

//	public void ROIChanged(SequenceChangeEvent e) {
//	}
//
//	public void dimensionChanged(SequenceChangeEvent e) {
//	}
//
//	public void nameChanged(SequenceChangeEvent e) {
//	}

//	public void selectionChanged(SequenceChangeEvent e) {
//		trackPool.fireTrackEditorProcessorChange();
//	}

//	@Override
//	protected void finalize() throws Throwable {
//		try
//		{
//			trackPool.getSequence().removeSequenceChangeListener( this );
//		}
//		catch ( NullPointerException e)
//		{
//			// the sequence do not exists anymore.
//		}
//		super.finalize();
//	}

	@Override
	public void Close() {}

	@Override
	public void displaySequenceChanged() {

		for ( Sequence sequence : Icy.getMainInterface().getSequences() )
		{
			sequence.removeListener( this );			
		}
		
		Sequence displaySequence = trackPool.getDisplaySequence();
		if ( displaySequence != null )
		{
			displaySequence.addListener( this );
		}
		
	}

	@Override
	public void sequenceChanged(SequenceEvent sequenceEvent) {
		
		for ( Viewer viewer : Icy.getMainInterface().getViewers() )
		{
			viewer.removeListener( this );			
		}

		try
		{
			trackPool.getDisplaySequence().getFirstViewer().addListener( this );
		}catch( Exception e )
		{
			// the viewer or sequence does not exist anymore 
		}
		
	}

	@Override
	public void sequenceClosed(Sequence sequence) {
		
	}

	@Override
	public void viewerChanged(ViewerEvent event) {
		if ( event.getType() == ViewerEventType.POSITION_CHANGED )
		{
			trackPool.fireTrackEditorProcessorChange();
		}
	}

	@Override
	public void viewerClosed(Viewer viewer) {

	}

}
