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
package ca.uqac.lif.cep.peg;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.peg.TrendDistance;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Bags;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;

/**
 * This processor class extends GroupProcessors. Its main goal is to calculate a
 * second order trend distance by taking multiple TrendDistance processors in
 * parallel, each calculating a different aspect of the original input (mean,
 * variance, covariance, etc.) and sending the output to an array of boolean
 * values. The output produced can then be used to calculate a second trend
 * which can send an alert if too many deviations on multiple parameters occur.
 * For example, secondDegreeTrendDistance processor calculates the mean,
 * variance and covariance. If the output produced in the form of an array
 * contains more than 1 True value, the connected trend distance processor can
 * raise an alert.
 * 
 * @author Alexandre Larouche
 */
public class SecondOrderTrendDistance extends GroupProcessor
{
  public SecondOrderTrendDistance(TrendDistance<?,?,?> sec_order, TrendDistance<?, ?, ?>... processors)
  {
    super(1, 1); // Build ancestor
    build(sec_order, processors); // Build method to make the groupProcessor
  }

  protected void build(TrendDistance<?,?,?> sec_order, TrendDistance<?, ?, ?>... processors)
  {
    int countProc = processors.length; // Will count the amount of processors I recieve as args
    Class<?>[] classes = new Class<?>[countProc]; // Create an array of classes
    // to have the right amount of inputs
    Fork fork = new Fork(countProc);
    countProc = 0;
    for (TrendDistance<?, ?, ?> p : processors) // Adds the processors to the group sequentially...
    {
      addProcessors(p);
      classes[countProc] = Boolean.class;
      countProc++;
    }

    ApplyFunction createArray = new ApplyFunction(new Bags.ToArray(classes));
    countProc = 0;

    // Associate the output of each fork pipe to the
    // input of each proc
    for (TrendDistance<?, ?, ?> p : processors) 
    { // and associate the output of each proc to ToArray function
      Connector.connect(fork, countProc, p, INPUT);
      Connector.connect(p, OUTPUT, createArray, countProc);
      countProc++;
    }
    Connector.connect(createArray, sec_order);
    addProcessors(fork, createArray, sec_order);
    associateInput(INPUT, fork, INPUT);
    associateOutput(OUTPUT, sec_order, OUTPUT);
  }
}