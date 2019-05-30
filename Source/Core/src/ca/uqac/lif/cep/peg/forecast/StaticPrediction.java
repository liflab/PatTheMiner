/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2019 Sylvain Hallé and friends

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.peg.forecast;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tmf.Window;
import ca.uqac.lif.cep.tmf.WindowFunction;
import ca.uqac.lif.cep.util.Maps;

/**
 * A workflow that computes a prediction based on a feature computed over a
 * sliding window of events.  
 * <p>
 * <img src="{@docRoot}/doc-files/Prediction.png" alt="Processor chain">
 */
public class StaticPrediction extends GroupProcessor
{
  /**
   * Creates a new instance of the static prediction workflow.
   * @param slicing A slicing function, which
   * associates each incoming event to a slice identifier <i>s</i> ∈ <i>S</i>
   * @param phi A feature extraction <strong>processor</strong>
   * φ : Σ<sup>m</sup> → <i>V</i>, which takes a
   * window of m successive events and computes a feature value <i>v</i> ∈ <i>V</i>
   * @param m A window width
   * @param pi A predictive processor π : <i>V</i> → <i>P</i>, which associates a
   * feature value <i>v</i> ∈ <i>V</i> to a prediction <i>p</i> ∈ <i>P</i>.
   */
  public StaticPrediction(Function slicing, Processor phi, int m, Function pi)
  {
    super(1, 1);
    notifySources(true);
    Window feature = new Window(phi, m);
    Slice slice = new Slice(slicing, feature);
    ApplyFunction af = new ApplyFunction(new Maps.ApplyAll(pi));
    Connector.connect(slice, af);
    addProcessors(slice, af);
    associateInput(0, slice, 0);
    associateOutput(0, af, 0);
  }
  
  /**
   * Creates a new instance of the static prediction workflow.
   * @param slicing A slicing function, which
   * associates each incoming event to a slice identifier <i>s</i> ∈ <i>S</i>
   * @param phi A feature extraction <strong>function</strong> φ : Σ<sup>m</sup> → <i>V</i>, which takes a
   * window of m successive events and computes a feature value <i>v</i> ∈ <i>V</i>
   * @param pi A predictive processor π : <i>V</i> → <i>P</i>, which associates a
   * feature value <i>v</i> ∈ <i>V</i> to a prediction <i>p</i> ∈ <i>P</i>.
   */
  public StaticPrediction(Function slicing, Function phi, Function pi)
  {
    super(1, 1);
    notifySources(true);
    WindowFunction feature = new WindowFunction(phi);
    Slice slice = new Slice(slicing, feature);
    ApplyFunction af = new ApplyFunction(new Maps.ApplyAll(pi));
    Connector.connect(slice, af);
    addProcessors(slice, af);
    associateInput(0, slice, 0);
    associateOutput(0, af, 0);
  }
}
