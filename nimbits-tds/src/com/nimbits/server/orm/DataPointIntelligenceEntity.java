/*
 * Copyright (c) 2010 Tonic Solutions LLC.
 *
 * http://www.nimbits.com
 *
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eitherexpress or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nimbits.server.orm;

import com.nimbits.client.enums.IntelligenceResultTarget;
import com.nimbits.client.model.intelligence.Intelligence;

import javax.jdo.annotations.*;

/**
 * Created by bsautner
 * User: benjamin
 * Date: 10/29/11
 * Time: 10:50 AM
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "false")
public class DataPointIntelligenceEntity implements Intelligence {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private com.google.appengine.api.datastore.Key id;

    @Deprecated
    @Persistent(mappedBy = "dataPointIntelligenceEntity")
    private DataPoint point;



    @Persistent
    private Boolean enabled;

    @Persistent
    private Integer resultTarget;

    @Persistent
    @Deprecated
    public Long targetPointId;

    @Persistent
    private String target;

    @Persistent
    private String input;

    @Persistent
    private String nodeId;

    @Persistent
    private String trigger;

    @Persistent
    private String uuid;

    @Persistent
    private Boolean resultsInPlainText;

    public DataPointIntelligenceEntity() {
    }


    public DataPointIntelligenceEntity(Intelligence intelligence) {
        this.enabled = intelligence.getEnabled();
        this.resultTarget = intelligence.getResultTarget().getCode();
        this.target = intelligence.getTarget();
        this.input = intelligence.getInput();
        this.nodeId = intelligence.getNodeId();
        this.resultsInPlainText = intelligence.getResultsInPlainText();
        this.trigger = intelligence.getTrigger();
        this.uuid = intelligence.getUUID();

    }


    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public String getTrigger() {
        return trigger;
    }

    @Override
    public void setTrigger(String trigger) {
      this.trigger = trigger;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public IntelligenceResultTarget getResultTarget() {
        return IntelligenceResultTarget.get(resultTarget);
    }

    @Override
    public void setResultTarget(final IntelligenceResultTarget resultTarget) {
        this.resultTarget = resultTarget.getCode();
    }

    @Override
    public String getInput() {
        return input == null ? "" : input;
    }

    @Override
    public void setInput(final String input) {
        this.input = input;
    }

    @Override
    public String getNodeId() {
        return nodeId == null ? "" : nodeId;
    }

    @Override
    public void setNodeId(final String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean getResultsInPlainText() {
        return resultsInPlainText;
    }

    @Override
    public void setResultsInPlainText(final boolean resultsInPlainText) {
        this.resultsInPlainText = resultsInPlainText;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
