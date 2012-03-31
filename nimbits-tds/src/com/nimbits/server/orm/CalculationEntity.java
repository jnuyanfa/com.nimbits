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

import com.nimbits.client.model.calculation.Calculation;

import javax.jdo.annotations.*;

/**
 * Created by bsautner
 * User: benjamin
 * Date: 12/24/11
 * Time: 4:40 PM
 */


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "false")

public class CalculationEntity implements Calculation {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private com.google.appengine.api.datastore.Key id;

    @Persistent
    private String formula;

    @Persistent
    private String trigger;

    @Persistent
    private Boolean enabled;

    @Persistent
    @Deprecated
    public Long target;

    @Persistent
    @Deprecated
    public Long x;

    @Persistent
    @Deprecated
    public Long y;

    @Persistent
    @Deprecated
    public Long z;

    @Persistent
    private String xVar;

    @Persistent
    private String yVar;

    @Persistent
    private String zVar;

    @Persistent
    private String targetVar;

    @Persistent
    private String uuid;

    @Deprecated
    @Persistent(mappedBy = "calculationEntity")
    private DataPoint point;

    public CalculationEntity() {
    }



    public CalculationEntity(Calculation calculation) {
        this.formula = calculation.getFormula();
        this.enabled = calculation.getEnabled();
        this.targetVar = calculation.getTarget();
        this.xVar = calculation.getX();
        this.yVar = calculation.getY();
        this.zVar = calculation.getZ();
        this.uuid = calculation.getUUID();
        this.trigger = calculation.getTrigger();
    }


    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getTrigger() {
        return this.trigger;
    }


    @Override
    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    @Override
    public Boolean getEnabled() {
        return enabled == null ? false : enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getTarget() {
        return targetVar;
    }

    public void setTarget(String target) {
        this.targetVar = target;
    }

    @Override
    public String getX() {
        return xVar;
    }

    public void setX(String x) {
        this.xVar = x;
    }

    @Override
    public String getY() {
        return yVar ;
    }

    public void setY(String y) {
        this.yVar  = y;
    }

    @Override
    public String getZ() {
        return zVar;
    }

    @Override
    public void setEnabled(boolean b) {
        this.enabled = b;
    }

    public void setZ(String z) {
        this.zVar = z;
    }
}
