/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package plugins.fab.trackmanager.processors;

import fr.icy.Icy;
import fr.icy.extension.plugin.annotation_.IcyPluginName;
import fr.icy.gui.viewer.Viewer;
import fr.icy.gui.viewer.ViewerEvent;
import fr.icy.gui.viewer.ViewerListener;
import fr.icy.model.sequence.Sequence;
import fr.icy.model.sequence.SequenceEvent;
import fr.icy.model.sequence.SequenceListener;
import plugins.fab.trackmanager.PluginTrackManagerProcessor;
import plugins.fab.trackmanager.TrackSegment;
import plugins.nchenouard.spot.Detection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Fabrice de Chaumont
 * <p>
 * WARNING: BETA Version
 */
@IcyPluginName("Track Clipper")
public class TrackProcessorTimeClip extends PluginTrackManagerProcessor implements ActionListener, SequenceListener, ViewerListener {

    JTextField clipTextField = new JTextField("  3");
    JCheckBox futureCheckBox = new JCheckBox("Display future.", false);
    JCheckBox nonActiveTracksCheckBox = new JCheckBox("Display non active tracks", true);

    public TrackProcessorTimeClip() {
        setName("Track Clipper");
//		super(trackPool , "Track Clipper");
//		setPanelHeight( 90 );

        panel.setLayout(new GridLayout(3, 1));
        final JPanel clipPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        clipPanel.add(new JLabel("Number of detection to show before & after current Detection"));
        clipPanel.add(clipTextField);
        panel.add(clipPanel);

        panel.add(futureCheckBox);
        futureCheckBox.addActionListener(this);

        panel.add(nonActiveTracksCheckBox);
        nonActiveTracksCheckBox.addActionListener(this);

        //trackPool.getSequence().addSequenceChangeListener( this );
    }

    @Override
    public void Compute() {
        final Sequence displaySequence = trackPool.getDisplaySequence();
        if (displaySequence != null) {
            displaySequence.removeListener(this);
            displaySequence.addListener(this);

            final Viewer viewer = displaySequence.getFirstViewer();
            if (viewer != null) {
                viewer.removeListener(this);
                viewer.addListener(this);
            }
        }

        if (isEnabled()) {
            double clipValue;// = 3;
            try {
                clipValue = Double.parseDouble(clipTextField.getText());
            }
            catch (final Exception e) {
                clipValue = 3;
            }

            //int t = trackPool.getDisplaySequence().getSelectedT();
            // Find selected T. TODO: manage multiple viewers. Work if only one viewer is active.
            final ArrayList<Viewer> viewerList = Icy.getMainInterface().getViewers(trackPool.getDisplaySequence());
            int t;// = 0;
            try {
                t = viewerList.getFirst().getPositionT();
            }
            catch (final IndexOutOfBoundsException | NullPointerException e) {
                t = 0;
            }

            if (nonActiveTracksCheckBox.isSelected())
                for (final Detection d : trackPool.getAllDetection()) {
                    if (d.getT() < t - clipValue) d.setEnabled(false);
                    if (d.getT() > t + clipValue) d.setEnabled(false);
                    if (!futureCheckBox.isSelected())
                        if (d.getT() > t) d.setEnabled(false);
                }
            else
                for (final TrackSegment ts : trackPool.getTrackSegmentList()) {
                    if (ts.getFirstDetection().getT() <= t && ts.getLastDetection().getT() >= t)
                        for (final Detection d : ts.getDetectionList()) {
                            if (d.getT() < t - clipValue) d.setEnabled(false);
                            if (d.getT() > t + clipValue) d.setEnabled(false);
                            if (!futureCheckBox.isSelected())
                                if (d.getT() > t) d.setEnabled(false);
                        }
                    else
                        ts.setAllDetectionEnabled(false);
                }
        }

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
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
    public void Close() {

    }

    @Override
    public void displaySequenceChanged() {

        for (final Sequence sequence : Icy.getMainInterface().getSequences()) {
            sequence.removeListener(this);
        }

        final Sequence displaySequence = trackPool.getDisplaySequence();
        if (displaySequence != null) {
            displaySequence.addListener(this);
        }

    }

    @Override
    public void sequenceChanged(final SequenceEvent sequenceEvent) {

        for (final Viewer viewer : Icy.getMainInterface().getViewers()) {
            viewer.removeListener(this);
        }

        try {
            trackPool.getDisplaySequence().getFirstViewer().addListener(this);
        }
        catch (final Exception e) {
            // the viewer or sequence does not exist anymore
        }

    }

    @Override
    public void sequenceClosed(final Sequence sequence) {

    }

    @Override
    public void viewerChanged(final ViewerEvent event) {
        if (event.getType() == ViewerEvent.ViewerEventType.POSITION_CHANGED) {
            trackPool.fireTrackEditorProcessorChange();
        }
    }

    @Override
    public void viewerClosed(final Viewer viewer) {

    }
}
