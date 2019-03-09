package io.github.rowak.nanoleafdesktop.shortcuts;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;

public class Action
{
	private ActionType type;
	private Object[] args;
	
	public Action(ActionType type, Object[] args)
	{
		this.type = type;
		this.args = args;
	}
	
	public ActionType getType()
	{
		return type;
	}
	
	public void setType(ActionType type)
	{
		this.type = type;
	}
	
	public Object[] getArgs()
	{
		return args;
	}
	
	public void setArgs(Object[] args)
	{
		this.args = args;
	}
	
	public void execute(Aurora device)
	{
		try
		{
			switch (type)
			{
				case DEVICE_ON:
					device.state().setOn(true);
					break;
				case DEVICE_OFF:
					device.state().setOn(false);
					break;
				case DEVICE_TOGGLE:
					device.state().toggleOn();
					break;
				case INCREASE_BRIGHTNESS:
					device.state().increaseBrightness((int)args[0]);
					break;
				case DECREASE_BRIGHTNESS:
					device.state().decreaseBrightness((int)args[0]);
					break;
				case INCREASE_COLOR_TEMP:
					device.state().setColorTemperature(device.state().getColorTemperature() + (int)args[0]);
					break;
				case DECREASE_COLOR_TEMP:
					device.state().setColorTemperature(device.state().getColorTemperature() + (int)args[0]);
					break;
				case SET_EFFECT:
					device.effects().setEffect((String)args[0]);
					break;
				case SET_BRIGHTNESS:
					device.state().setBrightness((int)args[0]);
					break;
				case SET_COLOR_TEMP:
					device.state().setColorTemperature((int)args[0]);
					break;
			}
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
}
