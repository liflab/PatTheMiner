package ca.uqac.lif.factory;

public interface ObjectFactory<T,U>
{
	public T getForm();
	
	public U getObject();
}
