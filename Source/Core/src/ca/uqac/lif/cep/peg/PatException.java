/*
    A BeepBeep palette for mining event traces
    Copyright (C) 2017 Sylvain Hallé

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

/**
 * Generic exception raised by the use of the library
 * @author Sylvain Hallé
 */
public class PatException extends Exception
{
	/**
	 * Dummy UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new PatException from another exception
	 * @param e The exception
	 */
	public PatException(Exception e)
	{
		super(e);
	}
	
	/**
   * Creates a new PatException from a throwable
   * @param t The throwable
   */
	public PatException(Throwable t)
	{
		super(t);
	}
}
