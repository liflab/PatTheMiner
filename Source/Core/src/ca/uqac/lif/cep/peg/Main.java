/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017-2018 Sylvain Hallé and friends

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

public class Main
{
  /**
   * Build string to identify versions
   */
  protected static final String VERSION_STRING = Main.class.getPackage().getImplementationVersion();
  
  /**
   * Main method
   * @param args Command line arguments
   */
  public static void main(String[] args)
  {
    System.out.println("Pat The Miner v" + VERSION_STRING + " - Trend computations for event streams");
    System.out.println("(C) 2017-2018 Laboratoire d'informatique formelle");
    System.out.println("This JAR file is a library that is not meant to be run from the");
    System.out.println("command line.");
    System.exit(0);
  }

  /**
   * Constructor. Should not be accessed.
   */
  private Main()
  {
    throw new IllegalAccessError("Main class");
  }
}