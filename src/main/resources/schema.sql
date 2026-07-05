-- 演示充电站数据
INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd1', '大庆万达广场充电站', '萨尔图区东风路15号万达广场B1', 125.1036, 46.5982, '特来电', 6, 3, 120, '["快充","慢充"]', 1.20, '06:00-23:00', '0459-6116789', 'open', '万达广场地下停车场'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd1');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd2', '萨尔图机场充电站', '萨尔图区机场路1号P1区', 125.1347, 46.7433, '国网电动', 4, 3, 60, '["快充"]', 1.50, '00:00-24:00', '0459-6390222', 'open', '24小时营业'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd2');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd3', '大庆东站充电站', '龙凤区龙凤大街东站南广场', 125.1281, 46.5436, '星星充电', 6, 3, 180, '["快充","超快充"]', 1.35, '00:00-24:00', '', 'open', '支持超快充'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd3');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd4', '让胡路昆仑大街充电站', '让胡路区昆仑大街68号', 124.8847, 46.6269, '国网电动', 4, 3, 60, '["慢充"]', 0.90, '08:00-20:00', '', 'open', '居民区充电'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd4');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd5', '大庆市政府充电站', '萨尔图区世纪大道市政府院内', 125.0382, 46.5897, '特来电', 3, 1, 120, '["快充","慢充"]', 1.10, '07:00-21:00', '', 'open', '市政府东侧停车场'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd5');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd6', '龙凤世纪大道充电站', '龙凤区世纪大道与龙永路交叉口', 125.1397, 46.5581, '星星充电', 3, 1, 120, '["快充","慢充"]', 1.25, '00:00-24:00', '', 'closed', '设备升级中'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd6');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd7', '红岗区充电站', '红岗区萨大路红岗商城', 124.9756, 46.3698, '国网电动', 3, 2, 60, '["慢充"]', 0.85, '06:00-22:00', '', 'open', '红岗商城西侧'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd7');

INSERT INTO stations (id, name, address, longitude, latitude, operator, total_spots, available_spots, power, charger_types, price, open_hours, phone, status, description)
SELECT 'd8', '大同区同阳路充电站', '大同区同阳路178号', 124.7983, 46.0487, '特来电', 3, 1, 120, '["快充","慢充"]', 1.15, '07:00-21:00', '', 'maintenance', '维护中'
WHERE NOT EXISTS (SELECT 1 FROM stations WHERE id = 'd8');

-- 演示充电桩数据
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p1_1', 'd1', '1号桩', '快充', 120, 1.20, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p1_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p1_2', 'd1', '2号桩', '快充', 120, 1.20, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p1_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p1_3', 'd1', '3号桩', '快充', 120, 1.20, 'in_use' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p1_3');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p1_4', 'd1', '4号桩', '慢充', 7, 0.90, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p1_4');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p1_5', 'd1', '5号桩', '慢充', 7, 0.90, 'maintenance' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p1_5');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p1_6', 'd1', '6号桩', '慢充', 7, 0.90, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p1_6');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p2_1', 'd2', '1号桩', '快充', 60, 1.50, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p2_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p2_2', 'd2', '2号桩', '快充', 60, 1.50, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p2_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p2_3', 'd2', '3号桩', '快充', 60, 1.50, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p2_3');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p2_4', 'd2', '4号桩', '快充', 60, 1.50, 'in_use' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p2_4');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p3_1', 'd3', '1号桩', '超快充', 180, 1.35, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p3_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p3_2', 'd3', '2号桩', '超快充', 180, 1.35, 'in_use' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p3_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p3_3', 'd3', '3号桩', '快充', 120, 1.20, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p3_3');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p3_4', 'd3', '4号桩', '快充', 120, 1.20, 'maintenance' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p3_4');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p3_5', 'd3', '5号桩', '快充', 120, 1.20, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p3_5');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p3_6', 'd3', '6号桩', '快充', 120, 1.20, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p3_6');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p4_1', 'd4', '1号桩', '慢充', 7, 0.90, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p4_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p4_2', 'd4', '2号桩', '慢充', 7, 0.90, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p4_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p4_3', 'd4', '3号桩', '慢充', 7, 0.90, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p4_3');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p4_4', 'd4', '4号桩', '慢充', 7, 0.90, 'closed' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p4_4');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p5_1', 'd5', '1号桩', '快充', 120, 1.10, 'in_use' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p5_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p5_2', 'd5', '2号桩', '快充', 120, 1.10, 'in_use' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p5_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p5_3', 'd5', '3号桩', '慢充', 7, 0.85, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p5_3');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p6_1', 'd6', '1号桩', '快充', 120, 1.25, 'closed' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p6_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p6_2', 'd6', '2号桩', '快充', 120, 1.25, 'closed' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p6_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p6_3', 'd6', '3号桩', '慢充', 7, 0.95, 'closed' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p6_3');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p7_1', 'd7', '1号桩', '慢充', 7, 0.85, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p7_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p7_2', 'd7', '2号桩', '慢充', 7, 0.85, 'open' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p7_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p7_3', 'd7', '3号桩', '慢充', 7, 0.85, 'in_use' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p7_3');

INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p8_1', 'd8', '1号桩', '快充', 120, 1.15, 'maintenance' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p8_1');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p8_2', 'd8', '2号桩', '快充', 120, 1.15, 'maintenance' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p8_2');
INSERT INTO piles (id, station_id, name, type, power, price, status)
SELECT 'p8_3', 'd8', '3号桩', '慢充', 7, 0.85, 'maintenance' WHERE NOT EXISTS (SELECT 1 FROM piles WHERE id = 'p8_3');
