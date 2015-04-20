package ch.eawag.chemeql;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


public class ProceedCancelDialogBeanInfo extends SimpleBeanInfo
{
	public static PropertyDescriptor property(String name, String descr)
			throws IntrospectionException {
		PropertyDescriptor p = new PropertyDescriptor(name, ProceedCancelDialog.class);
		p.setShortDescription(descr);
		return p;
	}

	private static BeanDescriptor beanDescriptor = null;
	private static PropertyDescriptor[] properties = null;

	public BeanDescriptor getBeanDescriptor() {
		if (beanDescriptor == null) {
			beanDescriptor = new BeanDescriptor(ProceedCancelDialog.class);
			beanDescriptor.setValue("containerDelegate", "getControls");
		}
		return beanDescriptor;
	}

	public BeanInfo[] getAdditionalBeanInfo() {
		try {
			return new BeanInfo[]{Introspector.getBeanInfo(
				javax.swing.JDialog.class, java.awt.Container.class)};
		} catch (IntrospectionException ex) {
			return new BeanInfo[0];
		}
	}

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			if (properties == null) {
				properties = new PropertyDescriptor[]{
					property("proceedButtonText", "Label for the proceed button"),
					property("cancelButtonText", "Label for the cancel button"),};
			}
			properties[0].setPreferred(true);
			return properties;
		} catch (IntrospectionException ex) {
			return super.getPropertyDescriptors();
		}
	}
}
