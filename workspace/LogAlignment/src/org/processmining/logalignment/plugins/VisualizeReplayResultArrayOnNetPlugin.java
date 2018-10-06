package org.processmining.logalignment.plugins;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.connections.ReplayEventLogArrayOnAcceptingPetriNetArrayConnection;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;

@Plugin(name = "@0 Visualize Replay Result Array using Nets", returnLabels = { "Visualized Replay Result Array" }, returnTypes = { JComponent.class }, parameterLabels = { "replay Result Array" }, userAccessible = true)
@Visualizer
public class VisualizeReplayResultArrayOnNetPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, ReplayResultArray replayResults) {

		EventLogArray logs = null;
		AcceptingPetriNetArray nets = null;
		ReplayEventLogArrayOnAcceptingPetriNetArrayConnection connection;
		try {
			connection = context.getConnectionManager().getFirstConnection(
					ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.class, context,
					replayResults);
			logs = connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.LOGS);
			nets = connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.NETS);
			JTabbedPane tabbedPane = new JTabbedPane();
			for (int index = 0; index < replayResults.getSize(); index++) {
				String label = "Replay " + (index + 1);
				tabbedPane.add(label, (new PNLogReplayProjectedVisPanel(context, nets.getNet(index).getNet(), nets.getNet(index).getInitialMarking(), logs.getLog(index), connection.getParameters().getMapping(), replayResults.getReplay(index))));
			}
			return tabbedPane;
		} catch (ConnectionCannotBeObtained e) {
			return new JLabel(e.getMessage());
		}
	}

}
