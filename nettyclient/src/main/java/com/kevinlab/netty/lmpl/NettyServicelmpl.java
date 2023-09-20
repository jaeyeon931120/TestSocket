package com.kevinlab.netty.lmpl;

import com.kevinlab.netty.NettyClientHandler;
import com.kevinlab.netty.service.NettyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NettyServicelmpl implements NettyService {

    private static final Logger logger = LogManager.getLogger(NettyClientHandler.class);

    private String timestirng(String hex){
        long decimal = Long.decode(hex);
        String timestring = String.valueOf((char)decimal);

        return timestring;
    }

    private String datastirng(String hex){
        String datastring = hex.replace("0x", "");

        return datastring;
    }

    private String errorstirng(String hex){
        String errorstring = hex.replace("0x", "");
        String error = errorstring.substring(errorstring.length() - 1);

        return error;
    }

    private String data(String hex){
        int decimal = Integer.parseInt(hex,16);
        String data = Integer.toString(decimal);

        return data;
    }

    private String meter_set(String hex){
        int decimal = Integer.parseInt(hex,16);
        String binary = Integer.toBinaryString(decimal);

        return binary;
    }

    private String float_set(byte[] b){
        int intBits = b[0] << 24
                | (b[1] & 0xFF) << 16
                | (b[2] & 0xFF) << 8
                | (b[3] & 0xFF);

        float hex = Float.intBitsToFloat(intBits);
        String result = String.valueOf(hex);

        return result;
    }

    @Override
    public Map<String, Object> protocol(String[] content,byte[] b) throws Exception{
        logger.info("프로토콜 접속 완료");
        Map<String, Object> electricdata = new HashMap<String, Object>();
        Map<String, Object> waterdata = new HashMap<String, Object>();
        Map<String, Object> hotwaterdata = new HashMap<String, Object>();
        Map<String, Object> gasdata = new HashMap<String, Object>();
        Map<String, Object> equipdata = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> returndata = new HashMap<String, Object>();

        try {
            int num1 = 0;
            int num2 = 0;
            int num3 = 0;
            int num4 = 0;
            int num5 = 0;
            String STX = "";
            String gw_id = "";
            String client_id = "";
            String site_code = "";
            String spare = "";
            String temp = "";
            String humidity = "";
            String time = "";
            String meter_set = "";
            String ETX = "";
            String CRC = "";

            for (int i = 0; i < content.length; i++) {
                if (i == 0) {
                    STX = content[i];
                } else if (i == 1) {
                    gw_id = content[i];
                } else if (2 <= i && i <= 5) {
                    client_id += datastirng(content[i]);
                } else if (6 <= i && i <= 9) {
                    site_code += datastirng(content[i]);
                } else if (i == 10) {
                    spare = content[i];
                } else if (11 <= i && i <= 12) {
                    temp += datastirng(content[i]);
                } else if (13 <= i && i <= 14) {
                    humidity += datastirng(content[i]);
                } else if (15 <= i && i <= 28) {
                    time += timestirng(content[i]);
                } else if (i == 29) {
                    meter_set = meter_set(datastirng(content[i]));
                } else if(i == 30){
                    electricdata = electric(content, i);
                    num1 = Integer.parseInt(electricdata.get("num").toString());
                } else if(num1 == i){
                    waterdata = water(content, i);
                    num2 = Integer.parseInt(waterdata.get("num").toString());
                } else if(num2 == i){
                    hotwaterdata = hotwater(content, i);
                    num3 = Integer.parseInt(hotwaterdata.get("num").toString());
                } else if(num3 == i){
                    gasdata = gas(content, i);
                    num4 = Integer.parseInt(gasdata.get("num").toString());
                } else if(num4 == i){
                    equipdata = equipment(content, i);
                    num5 = Integer.parseInt(equipdata.get("num").toString());
                } else if (i == content.length-3){
                    ETX = datastirng(content[i]);
                } else if (content.length-2 <= i && i <= content.length-1){
                    CRC += content[i];
                }
            }

            temp = temp.substring(0, temp.length()-2) + "." + temp.substring(temp.length()-2, temp.length());
            humidity = humidity.substring(0, humidity.length()-2) + "." + humidity.substring(humidity.length()-2, humidity.length());
            CRC = CRC.substring(0, humidity.length()-1) + " " + CRC.substring(humidity.length());

            data.put("sensor_id", data(site_code) + data(client_id));
            data.put("val_date", time);
            data.put("lora_hum", humidity);
            data.put("lora_temp", temp);

            if (electricdata != null) {
                electricdata.putAll(data);
                electricdata.remove("num");
                returndata.put("electric", electricdata);
            }
            if (equipdata != null) {
                data.put("in_temp", equipdata.get("in_temp"));
                data.put("out_temp", equipdata.get("out_temp"));
                if (equipdata.get("in_temp") != null) {
                    data.put("err_in_temp", 0);
                } else {
                    data.put("err_in_temp", 1);
                }
                if (equipdata.get("out_temp") != null) {
                    data.put("err_out_temp", 0);
                } else {
                    data.put("err_out_temp", 1);
                }
//                    data.put("err_bat", 0);

                if (waterdata != null) {
                    waterdata.putAll(data);
                    waterdata.put("flow", waterdata.get("val"));
                    waterdata.remove("num");
                    returndata.put("water", waterdata);
                }
                if (hotwaterdata != null) {
                    hotwaterdata.putAll(data);
                    hotwaterdata.put("flow", hotwaterdata.get("val"));
                    hotwaterdata.remove("num");
                    returndata.put("hotwater", hotwaterdata);
                }
                if (gasdata != null) {
                    gasdata.putAll(data);
                    gasdata.put("flow", gasdata.get("val"));
                    gasdata.remove("num");
                    returndata.put("gas", gasdata);
                }

                equipdata.putAll(data);
                equipdata.remove("num");
                returndata.put("heating", equipdata);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return returndata;
    }

    private Map<String, Object> electric(String[] content, int num){
        Map<String, Object> electricdata = new HashMap<String, Object>();
        String total_wh = "";
        String active_power = "";
        String err_wh = "";
        String errcon_wh = "";

        try {
            for(int i = num; i < content.length; i++){
                if(content.length == 83){
                    if(num <= i && i <= num+3){
                        total_wh += datastirng(content[i]);
                    } else if(num+4 <= i && i <= num+7){
//                    active_power = datastirng(content[i]);
                    } else if(i == num+8){
                        err_wh = errorstirng(content[i]);
                    } else if(i == num+9){
                        errcon_wh = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                } else if(content.length == 90) {
                    if(num <= i && i <= num+3){
                        total_wh += datastirng(content[i]);
                    } else if(num+4 <= i && i <= num+7){
//                    active_power = datastirng(content[i]);
                    } else if(num+8 <= i && i <= num+11){
                        err_wh = errorstirng(content[i]);
                    } else if(i == num+12){
                        errcon_wh = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                }
            }

            electricdata.put("total_wh", StringUtils.stripStart(data(total_wh), "0"));
            electricdata.put("w", StringUtils.stripStart(active_power, "0"));
            electricdata.put("err_code", err_wh);
            electricdata.put("err_code_com", errcon_wh);
            electricdata.put("num", num);

        } catch (Exception e){
            e.printStackTrace();
        }

        return electricdata;
    }

    private Map<String, Object> water(String[] content, int num){
        Map<String, Object> waterdata = new HashMap<String, Object>();
        String water = "";
        String start_water = "";
        String end_water = "";
        String err_water = "";
        String errcon_water = "";

        try {
            for(int i = num; i < content.length; i++){
                if(content.length == 83){
                    if(num <= i && i <= num+3){
                        water += datastirng(content[i]);
                    } else if(i == num+4){
                        start_water = datastirng(content[i]);
                    } else if(i == num+5){
                        end_water = datastirng(content[i]);
                    } else if(i == num+6){
                        err_water = errorstirng(content[i]);
                    } else if(i == num+7){
                        errcon_water = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                } else if(content.length == 90){
                    if(num <= i && i <= num+3){
                        water += datastirng(content[i]);
                    } else if(num+4 <= i && i <= num+7){
                        err_water += datastirng(content[i]);
                    } else if(i == num+8){
                        errcon_water = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                }
            }

            waterdata.put("val", StringUtils.stripStart(water, "0"));
            if(content.length == 83){
                waterdata.put("start_time", start_water);
                waterdata.put("stop_time", end_water);
            }
            waterdata.put("err_code", err_water);
            waterdata.put("err_code_com", errcon_water);
            waterdata.put("num", num);

        } catch (Exception e){
            e.printStackTrace();
        }

        return waterdata;
    }

    private Map<String, Object> hotwater(String[] content, int num){
        Map<String, Object> hotwaterdata = new HashMap<String, Object>();
        String hotwater = "";
        String start_hotwater = "";
        String end_hotwater = "";
        String err_hotwater = "";
        String errcon_hotwater = "";

        try {
            for(int i = num; i < content.length; i++){
                if(content.length == 83){
                    if(num <= i && i <= num+3){
                        hotwater += datastirng(content[i]);
                    } else if(i == num+4){
                        start_hotwater = datastirng(content[i]);
                    } else if(i == num+5){
                        end_hotwater = datastirng(content[i]);
                    } else if(i == num+6){
                        err_hotwater = errorstirng(content[i]);
                    } else if(i == num+7){
                        errcon_hotwater = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                } else if (content.length == 90) {
                    if(num <= i && i <= num+3){
                        hotwater += datastirng(content[i]);
                    } else if(num+4 <= i && i <= num+7){
                        err_hotwater += datastirng(content[i]);
                    } else if(i == num+8){
                        errcon_hotwater = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                }
            }

            hotwaterdata.put("val", StringUtils.stripStart(hotwater, "0"));
            if(content.length == 83){
                hotwaterdata.put("start_time", start_hotwater);
                hotwaterdata.put("stop_time", end_hotwater);
            }
            hotwaterdata.put("err_code", err_hotwater);
            hotwaterdata.put("err_code_com", errcon_hotwater);
            hotwaterdata.put("num", num);

        } catch (Exception e){
            e.printStackTrace();
        }

        return hotwaterdata;
    }

    private Map<String, Object> gas(String[] content, int num){
        Map<String, Object> gasdata = new HashMap<String, Object>();
        String gas = "";
        String start_gas = "";
        String end_gas = "";
        String err_gas = "";
        String errcon_gas = "";

        try {
            for(int i = num; i < content.length; i++){
                if(content.length == 83){
                    if(num <= i && i <= num+3){
                        gas += datastirng(content[i]);
                    } else if(i == num+4){
                        start_gas = datastirng(content[i]);
                    } else if(i == num+5){
                        end_gas = datastirng(content[i]);
                    } else if(i == num+6){
                        err_gas = errorstirng(content[i]);
                    } else if(i == num+7){
                        errcon_gas = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                } else if(content.length == 90){
                    if(num <= i && i <= num+3){
                        gas += datastirng(content[i]);
                    } else if(num+4 <= i && i <= num+7){
                        err_gas += datastirng(content[i]);
                    } else if(i == num+8){
                        errcon_gas = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                }
            }

            gasdata.put("val", StringUtils.stripStart(gas, "0"));
            if(content.length == 83){
                gasdata.put("start_time", start_gas);
                gasdata.put("stop_time", end_gas);
            }
            gasdata.put("err_code", err_gas);
            gasdata.put("err_code_com", errcon_gas);
            gasdata.put("num", num);

        } catch (Exception e){
            e.printStackTrace();
        }

        return gasdata;
    }

    private Map<String, Object> equipment(String[] content, int num){
        Map<String, Object> equipdata = new HashMap<String, Object>();
        String heat_capacity = "";
        String flux = "";
        String input_temp = "";
        String output_temp = "";
        String start_equip = "";
        String end_equip = "";
        String err_equip = "";
        String errcon_equip = "";

        try {
            for(int i = num; i < content.length; i++){
                if(content.length == 83) {
                    if (num <= i && i <= num + 3) {
                        heat_capacity += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        flux += datastirng(content[i]);
                    } else if (num + 8 <= i && i <= num + 9) {
                        input_temp += datastirng(content[i]);
                    } else if (num + 10 <= i && i <= num + 11) {
                        output_temp += datastirng(content[i]);
                    } else if (i == num + 12) {
                        start_equip = datastirng(content[i]);
                    } else if (i == num + 13) {
                        end_equip = datastirng(content[i]);
                    } else if (i == num + 14) {
                        err_equip = errorstirng(content[i]);
                    } else if (i == num + 15) {
                        errcon_equip = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                } else if(content.length == 90){
                    if(num <= i && i <= num+3){
                        heat_capacity += datastirng(content[i]);
                    } else if(num+4 <= i && i <= num+7){
                        flux += datastirng(content[i]);
                    } else if(num+8 <= i && i <= num+9){
                        input_temp += datastirng(content[i]);
                    } else if(num+10 <= i && i <= num+11){
                        output_temp += datastirng(content[i]);
                    } else if(num+12 <= i && i<= num+15){
                        err_equip = errorstirng(content[i]);
                    } else if(i == num+16){
                        errcon_equip = errorstirng(content[i]);
                        num = i+1;
                        break;
                    }
                }
            }

            input_temp = input_temp.substring(0, input_temp.length()-2) + "." + input_temp.substring(input_temp.length()-2, input_temp.length());
            output_temp = output_temp.substring(0, output_temp.length()-2) + "." + output_temp.substring(output_temp.length()-2, output_temp.length());

            equipdata.put("val", StringUtils.stripStart(heat_capacity, "0"));
            equipdata.put("flow", StringUtils.stripStart(flux, "0"));
            equipdata.put("in_temp", input_temp);
            equipdata.put("out_temp", output_temp);
            if(content.length == 83){
                equipdata.put("start_time", start_equip);
                equipdata.put("end_time", end_equip);
            }
            equipdata.put("err_code", err_equip);
            equipdata.put("err_code_com", errcon_equip);
            equipdata.put("num", num);

        } catch (Exception e){
            e.printStackTrace();
        }

        return equipdata;
    }
}
