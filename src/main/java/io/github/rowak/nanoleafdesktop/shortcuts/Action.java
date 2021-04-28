package io.github.rowak.nanoleafdesktop.shortcuts;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafapi.effectbuilder.StaticEffectBuilder;

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
	
	public Action(String type, String mode, String data)
	{
		this.type = getType(type, getMode(mode));
		if (type == null)
		{
			throw new IllegalArgumentException("Invalid action type");
		}
		if (type.equalsIgnoreCase("brightness") || type.equalsIgnoreCase("temp") ||
				type.equalsIgnoreCase("red") || type.equalsIgnoreCase("green") ||
				type.equalsIgnoreCase("blue") || type.equalsIgnoreCase("hue") ||
				type.equalsIgnoreCase("saturation"))
		{
			try
			{
				this.args = new Object[] {Integer.parseInt(data)};
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid value type");
			}
		}
		else
		{
			this.args = new Object[] {data};
		}
	}
	
	private ModeType getMode(String mode)
	{
		if (mode == null)
			return null;
		switch (mode.toLowerCase())
		{
			case "up": return ModeType.UP;
			case "down": return ModeType.DOWN;
			case "set": return ModeType.SET;
			default: return ModeType.NO_MODE;
		}
	}
	
	private ActionType getType(String type, ModeType mode)
	{
		if (type.equalsIgnoreCase("on"))
		{
			return ActionType.DEVICE_ON;
		}
		else if (type.equalsIgnoreCase("off"))
		{
			return ActionType.DEVICE_OFF;
		}
		else if (type.equalsIgnoreCase("toggle"))
		{
			return ActionType.DEVICE_TOGGLE;
		}
		else if (type.equalsIgnoreCase("brightness"))
		{
			if (mode == ModeType.UP) return ActionType.INCREASE_BRIGHTNESS;
			else if (mode == ModeType.DOWN) return ActionType.DECREASE_BRIGHTNESS;
			else if (mode == ModeType.SET) return ActionType.SET_BRIGHTNESS;
		}
		else if (type.equalsIgnoreCase("temp"))
		{
			if (mode == ModeType.UP) return ActionType.INCREASE_COLOR_TEMP;
			else if (mode == ModeType.DOWN) return ActionType.DECREASE_COLOR_TEMP;
			else if (mode == ModeType.SET) return ActionType.SET_COLOR_TEMP;
		}
		else if (type.equalsIgnoreCase("effect"))
		{
			return ActionType.SET_EFFECT;
		}
		else if (type.equalsIgnoreCase("hue"))
		{
			return ActionType.SET_HUE;
		}
		else if (type.equalsIgnoreCase("saturation"))
		{
			return ActionType.SET_SATURATION;
		}
		else if (type.equalsIgnoreCase("red"))
		{
			return ActionType.SET_RED;
		}
		else if (type.equalsIgnoreCase("green"))
		{
			return ActionType.SET_GREEN;
		}
		else if (type.equalsIgnoreCase("blue"))
		{
			return ActionType.SET_BLUE;
		}
		else if (type.equalsIgnoreCase("rgb"))
		{
			return ActionType.SET_RGB;
		}
		else if (type.equalsIgnoreCase("hsb"))
		{
			return ActionType.SET_HSB;
		}
		return null;
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
		if (type == null)
			return;
		
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
				case SET_RGB:
					previousState = devices[0].state().getColor();
					int[] cmp = getColorComponents((String)args[0]);
					if (cmp != null)
					{
						Color rgb = Color.fromRGB(cmp[0], cmp[1], cmp[2]);
						Frame frame = new Frame(rgb, 5);
						for (Aurora device : devices)
						{
							Effect color = new StaticEffectBuilder(device)
									.setAllPanels(frame).build("");
							device.effects().displayEffect(color);
						}
					}
					break;
				case SET_HSB:
					previousState = devices[0].state().getColor();
					cmp = getColorComponents((String)args[0]);
					if (cmp != null)
					{
						Color hsb = Color.fromHSB(cmp[0], cmp[1], cmp[2]);
						Frame frame = new Frame(hsb, 5);
						for (Aurora device : devices)
						{
							Effect color = new StaticEffectBuilder(device)
									.setAllPanels(frame).build("");
							device.effects().displayEffect(color);
						}
					}
					break;
			}
		}
		catch (StatusCodeException sce)
		{
//			sce.printStackTrace();
		}
	}
	
	private int[] getColorComponents(String str)
	{
		int[] cmp = new int[3];
		try
		{
			int offset = str.length() - 6;
			// x,y,z
			if (str.contains(","))
			{
				String[] cmpstr = str.split(",");
				for (int i = 0; i < cmpstr.length; i++)
				{
					cmp[i] = Integer.parseInt(cmpstr[i]);
				}
			}
			// #XXYYZZ or XXYYZZ
			else if (offset == 0 || offset == 1)
			{
				cmp[0] = hexToInt(str.substring(offset, 2+offset));
				cmp[1] = hexToInt(str.substring(2+offset, 4+offset));
				cmp[2] = hexToInt(str.substring(4+offset, 6+offset));
			}
			return cmp;
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
	
	private int hexToInt(String hex)
	{
		int n = 0;
		char c;
		for (int i = 0; i < hex.length(); i++)
		{
			c = hex.charAt(i);
			if (c >= '0' && c <= '9')
			{
				n += (c-'0')*Math.pow(10, (hex.length()-i-1));
			}
			else if (c >= 'A' && c <= 'Z')
			{
				n += (c-'A'+10)*Math.pow(16, (hex.length()-i-1));
			}
			else if (c >= 'a' && c <= 'z')
			{
				n += (c-'a'+10)*Math.pow(16, (hex.length()-i-1));
			}
		}
		return n;
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
				case SET_RGB:
					c = (Color)previousState;
					for (Aurora device : devices)
					{
						device.state().setColor(c);
					}
					break;
				case SET_HSB:
					c = (Color)previousState;
					for (Aurora device : devices)
					{
						device.state().setColor(c);
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
