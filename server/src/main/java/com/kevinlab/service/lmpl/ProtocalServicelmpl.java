package com.kevinlab.service.lmpl;

import com.kevinlab.mapper.DataMapper;
import com.kevinlab.service.ProtocalService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.util.*;

@Service("ProtocalService")
public class ProtocalServicelmpl implements ProtocalService {

    private final DataMapper mapper;

    public ProtocalServicelmpl(DataMapper mapper) {
        this.mapper = mapper;
    }

    private static final Logger logger = LogManager.getLogger(ProtocalServicelmpl.class);

    private String DBname() throws Exception {
        String resource = "properties/kevinlab.properties"; // properties설정파일위치
        Properties properties = new Properties(); // properties 읽어오기 위한 properties 객체 생성

        Reader reader = Resources.getResourceAsReader(resource); // properties 읽어오기
        properties.load(reader); // 읽은 값을 properties객체에 저장

        String dbnames[] = properties.getProperty("db.url").split("/"); // db url을 /로 구분하여 배열 생성

        String dbname = dbnames[dbnames.length - 1]; // 배열의 마지막 값을 변수에 저장

        return dbname;
    }

    private String timestirng(String hex) {
        long decimal = Long.decode(hex);
        String timestring = String.valueOf((char) decimal);

        return timestring;
    }

    private String datastirng(String hex) {
        String datastring = hex.replace("0x", "");

        return datastring;
    }

    private String errorstirng(String hex) {
        String errorstring = hex.replace("0x", "");
        String error = errorstring.substring(errorstring.length() - 1);

        return error;
    }

    private String data(String hex) {
        int decimal = Integer.parseInt(hex, 16);
        String data = Integer.toString(decimal);

        return data;
    }

    private String meter_set(String hex) {
        int decimal = Integer.parseInt(hex, 16);
        String binary = Integer.toBinaryString(decimal);

        return binary;
    }

    private String float_set(byte[] b) {
        int intBits = b[0] << 24
                | (b[1] & 0xFF) << 16
                | (b[2] & 0xFF) << 8
                | (b[3] & 0xFF);
        float hex = Float.intBitsToFloat(intBits);
        String result = String.valueOf(hex);

        return result;
    }

    private byte byte_cast(String content) {
        byte bc = Integer.decode(content).byteValue();

        return bc;
    }

    @Override
    public void protocals(String[] content) throws Exception {
        logger.info("프로토콜 접속 완료");
        Map<String, Object> electricdata = new HashMap<String, Object>();
        Map<String, Object> waterdata = new HashMap<String, Object>();
        Map<String, Object> hotwaterdata = new HashMap<String, Object>();
        Map<String, Object> gasdata = new HashMap<String, Object>();
        Map<String, Object> equipdata = new HashMap<String, Object>();
        Map<String, Object> sundata = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> datalist = new ArrayList<>();
        Map<String, Object> insertdata1 = new HashMap<String, Object>(); // 데이터를 정리하기 위한 맵
        Map<String, Object> insertdata2 = new HashMap<String, Object>(); // pk를 포함한 전체 컬럼 맵
        Map<String, Object> insertdata3 = new HashMap<String, Object>(); // insert를 하기위한 맵
        Map<String, Object> pkdata = new HashMap<String, Object>(); // insert를 하기위한 맵

        try {
            int electricnum = 0;
            int waternum = 0;
            int hotwaternum = 0;
            int gasnum = 0;
            int equipnum = 0;
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
                } else if (i == 30) {
                    electricdata = electric(content, i);
                    electricnum = Integer.parseInt(electricdata.get("num").toString());
                } else if (electricnum == i) {
                    waterdata = water(content, i);
                    waternum = Integer.parseInt(waterdata.get("num").toString());
                } else if (waternum == i) {
                    hotwaterdata = hotwater(content, i);
                    hotwaternum = Integer.parseInt(hotwaterdata.get("num").toString());
                } else if (hotwaternum == i) {
                    gasdata = gas(content, i);
                    gasnum = Integer.parseInt(gasdata.get("num").toString());
                } else if (gasnum == i) {
                    equipdata = equipment(content, i);
                    equipnum = Integer.parseInt(equipdata.get("num").toString());
                } else if (i == content.length - 3) {
                    ETX = datastirng(content[i]);
                } else if (content.length - 2 <= i && i <= content.length - 1) {
                    CRC += content[i];
                }
            }

            temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
            humidity = humidity.substring(0, humidity.length() - 2) + "." + humidity.substring(humidity.length() - 2, humidity.length());
            CRC = CRC.substring(0, humidity.length() - 1) + " " + CRC.substring(humidity.length());

            data.put("sensor_id", data(site_code) + data(client_id));
            data.put("val_date", time);
            data.put("lora_hum", humidity);
            data.put("lora_temp", temp);

            if (electricdata != null) {
                electricdata.putAll(data);
                sundata.putAll(electricdata);
                electricdata.put("tablename", "HM_DATA_RAW_6");
                sundata.put("tablename", "HM_DATA_RAW_7");
                electricdata.remove("num");
                electricdata.remove("err_code");
                sundata.remove("num");
                sundata.remove("w");
                sundata.remove("err_code");
                sundata.remove("lora_hum");
                sundata.remove("lora_temp");
                datalist.add(electricdata);
                datalist.add(sundata);
            }
            if (equipdata != null) {
                data.put("err_bat", 0);
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

                equipdata.putAll(data);
                equipdata.put("tablename", "HM_DATA_RAW_5");
                equipdata.remove("num");
                equipdata.remove("num");
                datalist.add(equipdata);

                data.put("in_temp", 0);
                data.put("out_temp", 0);

                if (waterdata != null) {
                    waterdata.putAll(data);
                    waterdata.put("flow", waterdata.get("val"));
                    waterdata.put("tablename", "HM_DATA_RAW_2");
                    waterdata.remove("num");
                    datalist.add(waterdata);
                }
                if (hotwaterdata != null) {
                    hotwaterdata.putAll(data);
                    hotwaterdata.put("flow", hotwaterdata.get("val"));
                    hotwaterdata.put("tablename", "HM_DATA_RAW_3");
                    hotwaterdata.remove("num");
                    datalist.add(hotwaterdata);
                }
                if (gasdata != null) {
                    gasdata.putAll(data);
                    gasdata.put("flow", gasdata.get("val"));
                    gasdata.put("tablename", "HM_DATA_RAW_4");
                    gasdata.remove("num");
                    datalist.add(gasdata);
                }
            }

            for (int i = 0; i < datalist.size(); i++) {
                String dbname = DBname();
                String tablename = datalist.get(i).get("tablename").toString();
                datalist.get(i).remove("tablename");

                /* 반복문마다 데이터가 겹치지 않기 위해서 맵 초기화를 한다. */
                insertdata1 = new HashMap<String, Object>(); // 맵 초기화
                insertdata2 = new HashMap<String, Object>(); // 맵 초기화
                pkdata = new HashMap<String, Object>(); // 맵 초기화

                /*
                 * 위에서 만든 맵을 key와 value를 각각 리스트로 만든다. 그리고 각각의 리스트를 맵에 저장한다.
                 */
                List<String> keyList = new ArrayList<>(datalist.get(i).keySet());
                List<Object> valueList = new ArrayList<>(datalist.get(i).values());
                List<Map<String, Object>> result1 = new ArrayList<Map<String, Object>>();
                List<Map<String, Object>> result2 = new ArrayList<Map<String, Object>>();

                for (int p = 0; p < keyList.size(); p++) {
                    insertdata1 = new HashMap<String, Object>();
                    pkdata = new HashMap<String, Object>();

                    insertdata1.put("name", keyList.get(p));
                    insertdata1.put("val", valueList.get(p));

                    if (!(keyList.get(p).equals("sensor_sn") || keyList.get(p).equals("val_date")
                            || keyList.get(p).equals("reg_date"))) {
                        pkdata.put("name", keyList.get(p));
                        pkdata.put("val", valueList.get(p));
                        result2.add(pkdata);
                    }

                    result1.add(insertdata1);
                }

                insertdata2.put("result1", result1);
                insertdata2.put("result2", result2);
                insertdata2.put("tablename", tablename);
                insertdata2.put("dbname", DBname());

                this.mapper.insert(insertdata2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> electric(String[] content, int num) {
        Map<String, Object> electricdata = new HashMap<String, Object>();
        String total_wh = "";
        String err_wh = "";
        String errcon_wh = "";
        byte[] active_power = new byte[4];
        int byte_num = 0;

        try {
            for (int i = num; i < content.length; i++) {
                if (content.length == 83) {
                    if (num <= i && i <= num + 3) {
                        total_wh += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        active_power[byte_num] = byte_cast(content[i]);
                        byte_num++;
                    } else if (i == num + 8) {
                        err_wh = errorstirng(content[i]);
                    } else if (i == num + 9) {
                        errcon_wh = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                } else if (content.length == 90) {
                    if (num <= i && i <= num + 3) {
                        total_wh += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        active_power[byte_num] = byte_cast(content[i]);
                        byte_num++;
                    } else if (num + 8 <= i && i <= num + 11) {
                        err_wh = errorstirng(content[i]);
                    } else if (i == num + 12) {
                        errcon_wh = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                }
            }

            electricdata.put("total_w", StringUtils.stripStart(data(total_wh), "0"));
            electricdata.put("w", float_set(active_power));
            electricdata.put("err_code", err_wh);
            electricdata.put("err_code_com", errcon_wh);
            electricdata.put("num", num);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return electricdata;
    }

    private Map<String, Object> water(String[] content, int num) {
        Map<String, Object> waterdata = new HashMap<String, Object>();
        String water = "";
        String start_water = "";
        String end_water = "";
        String err_water = "";
        String errcon_water = "";

        try {
            for (int i = num; i < content.length; i++) {
                if (content.length == 83) {
                    if (num <= i && i <= num + 3) {
                        water += datastirng(content[i]);
                    } else if (i == num + 4) {
                        start_water = datastirng(content[i]);
                    } else if (i == num + 5) {
                        end_water = datastirng(content[i]);
                    } else if (i == num + 6) {
                        err_water = errorstirng(content[i]);
                    } else if (i == num + 7) {
                        errcon_water = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                } else if (content.length == 90) {
                    if (num <= i && i <= num + 3) {
                        water += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        err_water += datastirng(content[i]);
                    } else if (i == num + 8) {
                        errcon_water = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                }
            }

            waterdata.put("val", StringUtils.stripStart(water, "0"));
            if (content.length == 83) {
                waterdata.put("start_time", start_water);
                waterdata.put("stop_time", end_water);
            }
            waterdata.put("err_code", err_water);
            waterdata.put("err_code_com", errcon_water);
            waterdata.put("num", num);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return waterdata;
    }

    private Map<String, Object> hotwater(String[] content, int num) {
        Map<String, Object> hotwaterdata = new HashMap<String, Object>();
        String hotwater = "";
        String start_hotwater = "";
        String end_hotwater = "";
        String err_hotwater = "";
        String errcon_hotwater = "";

        try {
            for (int i = num; i < content.length; i++) {
                if (content.length == 83) {
                    if (num <= i && i <= num + 3) {
                        hotwater += datastirng(content[i]);
                    } else if (i == num + 4) {
                        start_hotwater = datastirng(content[i]);
                    } else if (i == num + 5) {
                        end_hotwater = datastirng(content[i]);
                    } else if (i == num + 6) {
                        err_hotwater = errorstirng(content[i]);
                    } else if (i == num + 7) {
                        errcon_hotwater = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                } else if (content.length == 90) {
                    if (num <= i && i <= num + 3) {
                        hotwater += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        err_hotwater += datastirng(content[i]);
                    } else if (i == num + 8) {
                        errcon_hotwater = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                }
            }

            hotwaterdata.put("val", StringUtils.stripStart(hotwater, "0"));
            if (content.length == 83) {
                hotwaterdata.put("start_time", start_hotwater);
                hotwaterdata.put("stop_time", end_hotwater);
            }
            hotwaterdata.put("err_code", err_hotwater);
            hotwaterdata.put("err_code_com", errcon_hotwater);
            hotwaterdata.put("num", num);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hotwaterdata;
    }

    private Map<String, Object> gas(String[] content, int num) {
        Map<String, Object> gasdata = new HashMap<String, Object>();
        String gas = "";
        String start_gas = "";
        String end_gas = "";
        String err_gas = "";
        String errcon_gas = "";

        try {
            for (int i = num; i < content.length; i++) {
                if (content.length == 83) {
                    if (num <= i && i <= num + 3) {
                        gas += datastirng(content[i]);
                    } else if (i == num + 4) {
                        start_gas = datastirng(content[i]);
                    } else if (i == num + 5) {
                        end_gas = datastirng(content[i]);
                    } else if (i == num + 6) {
                        err_gas = errorstirng(content[i]);
                    } else if (i == num + 7) {
                        errcon_gas = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                } else if (content.length == 90) {
                    if (num <= i && i <= num + 3) {
                        gas += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        err_gas += datastirng(content[i]);
                    } else if (i == num + 8) {
                        errcon_gas = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                }
            }

            gasdata.put("val", StringUtils.stripStart(gas, "0"));
            if (content.length == 83) {
                gasdata.put("start_time", start_gas);
                gasdata.put("stop_time", end_gas);
            }
            gasdata.put("err_code", err_gas);
            gasdata.put("err_code_com", errcon_gas);
            gasdata.put("num", num);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return gasdata;
    }

    private Map<String, Object> equipment(String[] content, int num) {
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
            for (int i = num; i < content.length; i++) {
                if (content.length == 83) {
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
                        num = i + 1;
                        break;
                    }
                } else if (content.length == 90) {
                    if (num <= i && i <= num + 3) {
                        heat_capacity += datastirng(content[i]);
                    } else if (num + 4 <= i && i <= num + 7) {
                        flux += datastirng(content[i]);
                    } else if (num + 8 <= i && i <= num + 9) {
                        input_temp += datastirng(content[i]);
                    } else if (num + 10 <= i && i <= num + 11) {
                        output_temp += datastirng(content[i]);
                    } else if (num + 12 <= i && i <= num + 15) {
                        err_equip = errorstirng(content[i]);
                    } else if (i == num + 16) {
                        errcon_equip = errorstirng(content[i]);
                        num = i + 1;
                        break;
                    }
                }
            }

            input_temp = input_temp.substring(0, input_temp.length() - 2) + "." + input_temp.substring(input_temp.length() - 2, input_temp.length());
            output_temp = output_temp.substring(0, output_temp.length() - 2) + "." + output_temp.substring(output_temp.length() - 2, output_temp.length());

            equipdata.put("val", StringUtils.stripStart(heat_capacity, "0"));
            equipdata.put("flow", StringUtils.stripStart(flux, "0"));
            equipdata.put("in_temp", input_temp);
            equipdata.put("out_temp", output_temp);
            if (content.length == 83) {
                equipdata.put("start_time", start_equip);
                equipdata.put("stop_time", end_equip);
            }
            equipdata.put("err_code", err_equip);
            equipdata.put("err_code_com", errcon_equip);
            equipdata.put("num", num);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return equipdata;
    }
}
