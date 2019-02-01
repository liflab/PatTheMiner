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
import ca.uqac.lif.cep.Processor;

public class PredictedTrendDistance extends GroupProcessor
{
  public PredictedTrendDistance(Processor beta, Processor gamma, Processor rho, int n, int t)
  {
    super(1, 1);
  }
  
  protected static class PredictorGroup extends GroupProcessor
  {
    public PredictorGroup(Processor beta, Processor rho, int t)
    {
      super(1, 1);
      Connector.connect(beta, rho);
      
    }
  }
}
