package io.github.rowak.nanoleafdesktop.shortcuts;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.StatusCodeException;

public class Action
{
	private ActionType type;
	private Object[] args;
	private Object previousState;
	
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
	
	public Object getPreviousState()
	{
		return previousState;
	}
	
	public void execute(Aurora[] devices, Effect[] effects)
	{
		try
		{
			switch (type)
			{
				case DEVICE_ON:
					previousState = devices[0].state().getOn();
					for (Aurora device : devices)
					{
						device.state().setOn(true);
					}
					break;
				case DEVICE_OFF:
					previousState = devices[0].state().getOn();
					for (Aurora device : devices)
					{
						device.state().setOn(false);
					}
					break;
				case DEVICE_TOGGLE:
					previousState = devices[0].state().getOn();
					for (Aurora device : devices)
					{
						device.state().toggleOn();
					}
					break;
				case INCREASE_BRIGHTNESS:
					previousState = devices[0].state().getBrightness();
					for (Aurora device : devices)
					{
						device.state().increaseBrightness((int)args[0]);
					}
					break;
				case DECREASE_BRIGHTNESS:
					previousState = devices[0].state().getBrightness();
					for (Aurora device : devices)
					{
						device.state().decreaseBrightness((int)args[0]);
					}
					break;
				case SET_BRIGHTNESS:
					previousState = devices[0].state().getBrightness();
					for (Aurora device : devices)
					{
						device.state().setBrightness((int)args[0]);
					}
					break;
				case INCREASE_COLOR_TEMP:
					previousState = devices[0].state().getColorTemperature();
					for (Aurora device : devices)
					{
						device.state().setColorTemperature(device.state().getColorTemperature() + (int)args[0]);
					}
					break;
				case DECREASE_COLOR_TEMP:
					previousState = devices[0].state().getColorTemperature();
					for (Aurora device : devices)
					{
						device.state().setColorTemperature(device.state().getColorTemperature() + (int)args[0]);
					}
					break;
				case SET_COLOR_TEMP:
					previousState = devices[0].state().getColorTemperature();
					for (Aurora device : devices)
					{
						device.state().setColorTemperature((int)args[0]);
					}
					break;
				case SET_HUE:
					previousState = devices[0].state().getHue();
					for (Aurora device : devices)
					{
						device.state().setHue((int)args[0]);
					}
					break;
				case SET_SATURATION:
					previousState = devices[0].state().getSaturation();
					for (Aurora device : devices)
					{
						device.state().setSaturation((int)args[0]);
					}
					break;
				case SET_EFFECT:
					previousState = devices[0].effects().getCurrentEffectName();
					for (Aurora device : devices)
					{
						device.effects().setEffect((String)args[0]);
					}
					break;
				case NEXT_EFFECT:
					String[] effectNames = devices[0].effects().getEffectsList();
					String currentEffect = devices[0].effects().getCurrentEffectName();
					previousState = currentEffect;
					int nextEffect = -1;
					for (int i = 0; i < effects.length; i++)
					{
						if (currentEffect.equals(effects[i].getName()) && i+1 < effects.length)
						{
							if (!isRhythmEffect(effects[i+1]))
							{
								nextEffect = i+1;
							}
							else
							{
								int j = i+1;
								while (nextEffect == -1 && j < effects.length)
								{
									if (!isRhythmEffect(effects[j]))
									{
										nextEffect = j;
										break;
									}
									j++;
								}
							}
							break;
						}
					}
					for (Aurora device : devices)
					{
						if (nextEffect == -1)
						{
							device.effects().setEffect(effectNames[0]);
						}
						else
						{
							device.effects().setEffect(effectNames[nextEffect]);
						}
					}
					break;
				case PREVIOUS_EFFECT:
					effectNames = devices[0].effects().getEffectsList();
					currentEffect = devices[0].effects().getCurrentEffectName();
					previousState = currentEffect;
					nextEffect = -1;
					for (int i = 0; i < effects.length; i++)
					{
						if (currentEffect.equals(effects[i].getName()) && i-1 > -1)
						{
							if (!isRhythmEffect(effects[i-1]))
							{
								nextEffect = i-1;
							}
							else
							{
								int j = i-1;
								while (nextEffect == -1 && j > -1)
								{
									if (!isRhythmEffect(effects[j]))
									{
										nextEffect = j;
										break;
									}
									j--;
								}
							}
							break;
						}
					}
					for (Aurora device : devices)
					{
						if (nextEffect == -1)
						{
							device.effects().setEffect(effectNames[effectNames.length-1]);
						}
						else
						{
							device.effects().setEffect(effectNames[nextEffect]);
						}
					}
					break;
				case SET_RED:
					Color c = devices[0].state().getColor();
					previousState = c;
					for (Aurora device : devices)
					{
						device.state().setColor(Color.fromRGB((int)args[0], c.getGreen(), c.getBlue()));
					}
					break;
				case SET_GREEN:
					c = devices[0].state().getColor();
					previousState = c;
					for (Aurora device : devices)
					{
						device.state().setColor(Color.fromRGB(c.getRed(), (int)args[0], c.getBlue()));
					}
					break;
				case SET_BLUE:
					c = devices[0].state().getColor();
					previousState = c;
					for (Aurora device : devices)
					{
						device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), (int)args[0]));
					}
					break;
			}
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
	
	public void reset(Aurora[] devices, Effect[] effects)
	{
		try
		{
			switch (type)
			{
				case DEVICE_ON:
					for (Aurora device : devices)
					{
						device.state().setOn((boolean)previousState);
					}
					break;
				case DEVICE_OFF:
					for (Aurora device : devices)
					{
						device.state().setOn((boolean)previousState);
					}
					break;
				case DEVICE_TOGGLE:
					for (Aurora device : devices)
					{
						device.state().setOn((boolean)previousState);
					}
					break;
				case INCREASE_BRIGHTNESS:
					for (Aurora device : devices)
					{
						device.state().increaseBrightness((int)previousState);
					}
					break;
				case DECREASE_BRIGHTNESS:
					for (Aurora device : devices)
					{
						device.state().decreaseBrightness((int)previousState);
					}
					break;
				case SET_BRIGHTNESS:
					for (Aurora device : devices)
					{
						device.state().setBrightness((int)previousState);
					}
					break;
				case INCREASE_COLOR_TEMP:
					for (Aurora device : devices)
					{
						device.state().setColorTemperature((int)previousState);
					}
					break;
				case DECREASE_COLOR_TEMP:
					for (Aurora device : devices)
					{
						device.state().setColorTemperature((int)previousState);
					}
					break;
				case SET_COLOR_TEMP:
					for (Aurora device : devices)
					{
						device.state().setColorTemperature((int)previousState);
					}
					break;
				case SET_HUE:
					for (Aurora device : devices)
					{
						device.state().setHue((int)previousState);
					}
					break;
				case SET_SATURATION:
					for (Aurora device : devices)
					{
						device.state().setSaturation((int)previousState);
					}
					break;
				case SET_EFFECT:
					for (Aurora device : devices)
					{
						device.effects().setEffect((String)previousState);
					}
					break;
				case NEXT_EFFECT:
					for (Aurora device : devices)
					{
						device.effects().setEffect((String)previousState);
					}
					break;
				case PREVIOUS_EFFECT:
					for (Aurora device : devices)
					{
						device.effects().setEffect((String)previousState);
					}
					break;
				case SET_RED:
					Color c = (Color)previousState;
					for (Aurora device : devices)
					{
						device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					}
					break;
				case SET_GREEN:
					c = (Color)previousState;
					for (Aurora device : devices)
					{
						device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					}
					break;
				case SET_BLUE:
					c = (Color)previousState;
					for (Aurora device : devices)
					{
						device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					}
					break;
			}
			previousState = null;
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
	
	private boolean isRhythmEffect(Effect ef)
	{
		return ef.getAnimType() == Effect.Type.PLUGIN && ef.getPluginType().equals("rhythm");
	}
}
