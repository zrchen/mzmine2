/*
 * Copyright 2006-2009 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peakpicking.chromatogrambuilder.massfilters.shoulderpeaksfilter;

import net.sf.mzmine.modules.peakpicking.chromatogrambuilder.massfilters.shoulderpeaksfilter.peakmodels.GaussPeak;
import net.sf.mzmine.modules.peakpicking.chromatogrambuilder.massfilters.shoulderpeaksfilter.peakmodels.LorentzianPeak;
import net.sf.mzmine.modules.peakpicking.chromatogrambuilder.massfilters.shoulderpeaksfilter.peakmodels.LorentzianPeakWithShoulder;

public enum PeakModelType {

    GAUSS("Gaussian", GaussPeak.class),
    LORENTZ("Lorentzian", LorentzianPeak.class),
    LORENTZEXTENDED("Lorentzian extended", LorentzianPeakWithShoulder.class);

    private final String modelName;
    private final Class modelClass;

    PeakModelType(String modelName, Class modelClass) {
        this.modelName = modelName;
        this.modelClass = modelClass;
    }

    public String toString() {
        return modelName;
    }

    public Class getModelClass() {
        return modelClass;
    }

}

