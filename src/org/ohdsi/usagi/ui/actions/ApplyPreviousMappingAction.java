/*******************************************************************************
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohdsi.usagi.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ohdsi.usagi.CodeMapping;
import org.ohdsi.usagi.TargetConcept;
import org.ohdsi.usagi.ui.Global;
import org.ohdsi.usagi.ui.Mapping;

public class ApplyPreviousMappingAction extends AbstractAction {

	private static final long	serialVersionUID	= 3420357922150237898L;

	public ApplyPreviousMappingAction() {
		putValue(Action.NAME, "Apply previous mapping");
		putValue(Action.SHORT_DESCRIPTION, "Apply previous mapping to current code set");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JFileChooser fileChooser = new JFileChooser(Global.folder);
		FileFilter csvFilter = new FileNameExtensionFilter("CSV files", "csv");
		fileChooser.setFileFilter(csvFilter);
		if (fileChooser.showOpenDialog(Global.frame) == JFileChooser.APPROVE_OPTION) {
			int mappingsUsed = 0;
			int mappingsFailed = 0;
			File file = fileChooser.getSelectedFile();
			Mapping mapping = new Mapping();
			mapping.loadFromFile(file.getAbsolutePath());
			Map<String, List<TargetConcept>> codeToTargetConcept = new HashMap<String, List<TargetConcept>>();
			for (CodeMapping codeMapping : mapping)
				if (codeMapping.mappingStatus.equals(CodeMapping.MappingStatus.APPROVED))
					codeToTargetConcept.put(codeMapping.sourceCode.sourceCode, codeMapping.targetConcepts);

			for (CodeMapping codeMapping : Global.mapping) {
				List<TargetConcept> targetConcepts = codeToTargetConcept.get(codeMapping.sourceCode.sourceCode);
				if (targetConcepts != null) {
					List<TargetConcept> newTargetConcepts = new ArrayList<TargetConcept>(targetConcepts.size());
					for (TargetConcept targetConcept : targetConcepts) {
						TargetConcept newTargetConcept = Global.usagiSearchEngine.getTargetConcept(targetConcept.conceptId);
						if (newTargetConcept == null)
							mappingsFailed++;
						else
							newTargetConcepts.add(newTargetConcept);

					}
					if (newTargetConcepts.size() > 0) {
						codeMapping.targetConcepts = newTargetConcepts;
						mappingsUsed++;
					}
					if (newTargetConcepts.size() == targetConcepts.size()) 
						codeMapping.mappingStatus = CodeMapping.MappingStatus.APPROVED;
				}
			}
			String message = "The old mapping contained " + codeToTargetConcept.size() + " approved mappings of which " + mappingsUsed
					+ " were applied to the current mapping. " + mappingsFailed + " old target concepts could not be found in the new vocabulary.";
			Global.mappingTablePanel.updateUI();
			Global.mappingDetailPanel.updateUI();
			JOptionPane.showMessageDialog(Global.frame, message);
		}
	}

}
