package io.github.rowak.nanoleafdesktop.shortcuts;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Effect;
import io.github.rowak.StatusCodeException;

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
	
	public void execute(Aurora device, Effect[] effects)
	{
		try
		{
			switch (type)
			{
				case DEVICE_ON:
					previousState = device.state().getOn();
					device.state().setOn(true);
					break;
				case DEVICE_OFF:
					previousState = device.state().getOn();
					device.state().setOn(false);
					break;
				case DEVICE_TOGGLE:
					previousState = device.state().getOn();
					device.state().toggleOn();
					break;
				case INCREASE_BRIGHTNESS:
					previousState = device.state().getBrightness();
					device.state().increaseBrightness((int)args[0]);
					break;
				case DECREASE_BRIGHTNESS:
					previousState = device.state().getBrightness();
					device.state().decreaseBrightness((int)args[0]);
					break;
				case SET_BRIGHTNESS:
					previousState = device.state().getBrightness();
					device.state().setBrightness((int)args[0]);
					break;
				case INCREASE_COLOR_TEMP:
					previousState = device.state().getColorTemperature();
					device.state().setColorTemperature(device.state().getColorTemperature() + (int)args[0]);
					break;
				case DECREASE_COLOR_TEMP:
					previousState = device.state().getColorTemperature();
					device.state().setColorTemperature(device.state().getColorTemperature() + (int)args[0]);
					break;
				case SET_COLOR_TEMP:
					previousState = device.state().getColorTemperature();
					device.state().setColorTemperature((int)args[0]);
					break;
				case SET_HUE:
					previousState = device.state().getHue();
					device.state().setHue((int)args[0]);
					break;
				case SET_SATURATION:
					previousState = device.state().getSaturation();
					device.state().setSaturation((int)args[0]);
					break;
				case SET_EFFECT:
					previousState = device.effects().getCurrentEffectName();
					device.effects().setEffect((String)args[0]);
					break;
				case NEXT_EFFECT:
					String[] effectNames = device.effects().getEffectsList();
					String currentEffect = device.effects().getCurrentEffectName();
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
					if (nextEffect == -1)
					{
						device.effects().setEffect(effectNames[0]);
					}
					else
					{
						device.effects().setEffect(effectNames[nextEffect]);
					}
					break;
				case PREVIOUS_EFFECT:
					effectNames = device.effects().getEffectsList();
					currentEffect = device.effects().getCurrentEffectName();
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
					if (nextEffect == -1)
					{
						device.effects().setEffect(effectNames[effectNames.length-1]);
					}
					else
					{
						device.effects().setEffect(effectNames[nextEffect]);
					}
					break;
				case SET_RED:
					Color c = device.state().getColor();
					previousState = c;
					device.state().setColor(Color.fromRGB((int)args[0], c.getGreen(), c.getBlue()));
					break;
				case SET_GREEN:
					c = device.state().getColor();
					previousState = c;
					device.state().setColor(Color.fromRGB(c.getRed(), (int)args[0], c.getBlue()));
					break;
				case SET_BLUE:
					c = device.state().getColor();
					previousState = c;
					device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), (int)args[0]));
					break;
			}
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
	
	public void reset(Aurora device, Effect[] effects)
	{
		try
		{
			switch (type)
			{
				case DEVICE_ON:
					device.state().setOn((boolean)previousState);
					break;
				case DEVICE_OFF:
					device.state().setOn((boolean)previousState);
					break;
				case DEVICE_TOGGLE:
					device.state().setOn((boolean)previousState);
					break;
				case INCREASE_BRIGHTNESS:
					device.state().increaseBrightness((int)previousState);
					break;
				case DECREASE_BRIGHTNESS:
					device.state().decreaseBrightness((int)previousState);
					break;
				case SET_BRIGHTNESS:
					device.state().setBrightness((int)previousState);
					break;
				case INCREASE_COLOR_TEMP:
					device.state().setColorTemperature((int)previousState);
					break;
				case DECREASE_COLOR_TEMP:
					device.state().setColorTemperature((int)previousState);
					break;
				case SET_COLOR_TEMP:
					device.state().setColorTemperature((int)previousState);
					break;
				case SET_HUE:
					device.state().setHue((int)previousState);
					break;
				case SET_SATURATION:
					device.state().setSaturation((int)previousState);
					break;
				case SET_EFFECT:
					device.effects().setEffect((String)previousState);
					break;
				case NEXT_EFFECT:
					device.effects().setEffect((String)previousState);
					break;
				case PREVIOUS_EFFECT:
					device.effects().setEffect((String)previousState);
					break;
				case SET_RED:
					Color c = (Color)previousState;
					device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					break;
				case SET_GREEN:
					c = (Color)previousState;
					device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					break;
				case SET_BLUE:
					c = (Color)previousState;
					device.state().setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
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
