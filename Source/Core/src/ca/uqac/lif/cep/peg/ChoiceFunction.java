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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Choice function - takes as constructor parameters t, u, base and map. t =
 * input type (a.k.a. type of the keys inside the HashMap that will be passed) u
 * = output type (a.k.a. type of the values inside the HashMAp that will be
 * passed) base = the function which will compare keys in order to return a
 * value map = HashMap that is passed that will live inside the function's body.
 * <p>
 * The Choice Function should take a value that is of the same type as the map's
 * keys and compare this value with each key. Depending on the base function, it
 * can then return the value associated with the picked key.
 * <p>
 * An example to illustrate - Choice Function receives a time stamp - 4PM. It
 * will then compare this context to each and every key in the map (say the map
 * has two keys which are intervals, 8AM-5PM and 5PM-8AM). Also, let's say our
 * base function is one which find the closest key and returns it. We can then
 * find the key which resembles most 4PM and return this key's value as output.
 * 
 * [8AM-5PM] => 1000 [5PM-8AM] => 500
 * 
 * In this case, the base function would return the key [8AM-5PM] and the
 * ChoiceFunction would send 1000 as output to the processor.
 * 
 * @author Alexandre Larouche
 */
@SuppressWarnings("rawtypes")
public class ChoiceFunction<Q, R> extends UnaryFunction
{
  protected HashMap<Q, R> m_map;
  protected Calendar cal;

  @SuppressWarnings("unchecked")
  public ChoiceFunction(Class t, Class u, HashMap map)
  {
    super(t, u);
    m_map = map;
    cal = new GregorianCalendar();
  }

  @Override
  public Object getValue(Object x)
  {
    // Verify if the date sent is a day of the weekend or a work day, send the value
    // of the key accordingly.
    if (isWeekend((String) x))
    {
      return m_map.get("Weekend");
    }
    else
    {
      return m_map.get("Workday");
    }
  }

  /**
   * Parse date and figure out if it's a weekend day.
   * @param dt A string representing the day
   * @return true if the date is a weekend day, false if not
   */ 
  public boolean isWeekend(String dt)
  {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
    Date date = null;
    try
    {
      date = formatter.parse(dt);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    cal.setTime(date);
    int day = cal.get(Calendar.DAY_OF_WEEK);
    return day == Calendar.SUNDAY || day == Calendar.SATURDAY;
  }
}
