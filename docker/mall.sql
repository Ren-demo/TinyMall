/*
 Navicat Premium Dump SQL

 Source Server         : dianshang
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 8.137.168.204:3306
 Source Schema         : mall

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 17/03/2026 08:22:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `GoodsID` int NOT NULL AUTO_INCREMENT,
  `GoodsName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `StoreID` int NOT NULL,
  `Text` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Count` int NULL DEFAULT NULL,
  `Price` decimal(10, 2) NULL DEFAULT NULL,
  `Picture` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`GoodsID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 179 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, '晨光中性笔0.5mm黑色', 1, '办公学生专用中性笔，书写顺滑，大容量笔芯', 468, 2.50, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (2, '晨光笔记本A5加厚', 1, '100页加厚笔记本，米黄护眼纸，缝线装订不掉页', 0, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (3, '晨光固体胶高粘度', 1, '儿童手工办公固体胶，无甲醛，粘性强', 0, 1.80, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (4, '晨光文件夹A4单夹', 1, '加厚PP材质文件夹，防水耐用，办公整理必备', 90, 5.50, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (5, '东北五常大米5kg', 2, '当季新米，软糯香甜，真空包装锁鲜', 183, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (6, '山西黄小米2kg', 2, '农家小米，米油丰富，养胃早餐粥首选', 148, 25.80, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (7, '有机红豆1kg', 2, '无添加红小豆，颗粒饱满，煮粥煲汤佳品', 167, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (8, '黑米2.5kg', 2, '东北黑米，花青素丰富，杂粮饭搭档', 145, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (9, '新鲜生菜500g', 3, '现摘生菜，脆嫩爽口，沙拉/涮锅必备', 300, 3.50, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (10, '精品西红柿1kg', 3, '沙瓤西红柿，自然成熟，酸甜多汁', 250, 5.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (11, '鲜切五花肉500g', 3, '散养黑猪肉五花肉，肥瘦相间，烧烤炒菜', 100, 28.80, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (12, '基围虾400g', 3, '鲜活速冻基围虾，个头均匀，Q弹鲜美', 80, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (13, '纯棉短袖T恤男XL', 4, '100%纯棉短袖，宽松版型，透气吸汗', 120, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (14, '休闲牛仔裤男32码', 4, '弹力牛仔裤，直筒版型，不挑身材', 80, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (15, '男士运动外套L码', 4, '防风防水运动外套，立领设计，春秋款', 59, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (16, '补水保湿面膜10片', 5, '玻尿酸补水面膜，提亮肤色，深层锁水', 200, 79.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (17, '丝绒哑光口红#999', 5, '经典正红色口红，哑光质地，不拔干', 150, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (18, '氨基酸洁面乳100g', 5, '温和氨基酸洗面奶，深层清洁不紧绷', 180, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (19, '隔离霜妆前乳30ml', 5, '提亮遮瑕隔离霜，隐形毛孔，持妆久', 160, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (20, '家用纯棉毛巾2条装', 6, '新疆长绒棉毛巾，吸水速干，柔软亲肤', 250, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (21, '厨房清洁海绵10个', 6, '双面清洁海绵，去污不沾油，耐用不掉屑', 300, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (22, '防滑拖鞋居家款', 6, 'EVA材质拖鞋，轻便防滑，四季通用', 200, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (23, '原味珍珠奶茶粉1kg', 7, '商用奶茶粉，口感醇厚，冲调方便', 100, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (24, '焦糖珍珠500g', 7, 'Q弹黑糖珍珠，煮制简单，奶茶专用', 150, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (25, '冻干柠檬片20包', 7, '独立包装柠檬片，泡水喝补充维C', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (26, '蜂蜜柚子茶500g', 7, '冲调饮品，酸甜解腻，秋冬热饮首选', 180, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (27, '网红辣条大礼包', 8, '混合口味辣条，儿时味道，独立包装', 250, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (28, '酥脆薯片原味10包', 8, '非油炸薯片，薄脆鲜香，休闲零食', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (29, '牛肉味兰花豆500g', 8, '酥脆兰花豆，下酒追剧零食，颗粒饱满', 180, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (30, '阳光玫瑰葡萄1.5kg', 9, '无籽晴王葡萄，脆甜多汁，果香浓郁', 80, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (31, '丹东99草莓500g', 9, '奶油草莓，香甜软糯，现摘现发', 100, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (32, '赣南脐橙5kg', 9, '新鲜脐橙，皮薄多汁，酸甜可口', 150, 45.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (33, '进口车厘子JJ级1kg', 9, '大果车厘子，脆甜无渣，冷链运输', 60, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (34, '婴儿纯棉纸尿裤XL码', 10, '超薄透气纸尿裤，防侧漏，亲肤不红屁屁', 120, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (35, '宝宝辅食米粉400g', 10, '高铁米粉，6个月+宝宝辅食，易冲泡', 150, 45.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (36, '婴儿湿巾80抽', 10, '无酒精湿巾，加厚珍珠纹，手口专用', 200, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (37, '儿童保温杯350ml', 10, '316不锈钢保温杯，防漏便携，带吸管', 100, 79.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (38, '不锈钢螺丝刀套装', 11, '十字一字螺丝刀，防滑手柄，家用维修', 180, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (39, '膨胀螺丝M8*80mm', 11, '镀锌膨胀螺丝，承重强，安装牢固', 500, 0.80, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (40, 'PVC水管胶带5卷', 11, '防水胶带，耐腐蚀，水管补漏专用', 300, 5.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (41, '正宗郫县豆瓣酱500g', 12, '红油豆瓣酱，川菜之魂，炒菜调味必备', 200, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (42, '四川火锅底料200g', 12, '牛油火锅底料，麻辣鲜香，一料多用', 180, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (43, '泡豇豆300g', 12, '酸豆角，开胃下饭，炒肉末绝配', 250, 6.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (44, '干辣椒段250g', 12, '四川二荆条干辣椒，香辣够味', 0, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (45, '食用盐加碘500g*5包', 13, '深井岩盐，加碘食用盐，家用必备', 300, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (46, '生抽酱油1.28L', 13, '酿造生抽，提鲜调味，凉拌炒菜均可', 250, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (47, '卷纸卫生纸10卷', 13, '原木浆卷纸，四层加厚，无荧光剂', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (48, '台式卤肉饭料理包', 14, '加热即食料理包，卤肉香糯，方便快捷', 150, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (49, '酸辣粉速食6桶装', 14, '免煮酸辣粉，麻辣鲜香，懒人速食', 200, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (50, '自热米饭红烧牛肉味', 14, '自热米饭，15分钟即食，户外便携', 180, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (51, '方便面经典红烧味5包', 14, '经典泡面，面饼劲道，汤汁浓郁', 300, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (52, '无线蓝牙耳机', 15, '超长续航蓝牙耳机，降噪高清通话', 120, 199.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (53, '手机钢化膜2片装', 15, '全屏覆盖钢化膜，防摔防刮，高清透光', 250, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (54, '20W快充充电器', 15, '兼容多机型快充头，充电不发烫', 200, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (55, '针织开衫女中长款', 16, '软糯针织开衫，宽松显瘦，春秋外搭', 100, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (56, '高腰显瘦阔腿裤', 16, '垂感阔腿裤，遮肉显高，百搭款', 80, 79.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (57, '碎花雪纺连衣裙', 16, '法式碎花裙，收腰显瘦，夏季新款', 90, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (58, '纯棉打底衫女', 16, '修身打底衫，弹力大，多色可选', 150, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (59, '茉莉花茶50g罐装', 17, '新茶茉莉花茶，香气浓郁，耐泡', 180, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (60, '玻璃泡茶杯350ml', 17, '带滤网玻璃杯，耐热防爆，办公家用', 200, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (61, '普洱茶熟茶饼357g', 17, '云南普洱熟茶，口感醇厚，陈香浓郁', 80, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (62, '冷冻带鱼段1kg', 18, '深海带鱼段，去头去尾，肉质鲜嫩', 150, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (63, '扇贝肉500g', 18, '无沙扇贝肉，冷冻锁鲜，烧烤蒜蓉均可', 120, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (64, '鱿鱼须400g', 18, '新鲜鱿鱼须，脆嫩Q弹，火锅烧烤食材', 100, 25.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (65, '冷冻虾仁250g', 18, '去壳去线虾仁，鲜冻锁鲜，无冰衣', 130, 35.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (66, '卤香鸭脖200g', 19, '真空包装鸭脖，麻辣鲜香，肉质紧实', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (67, '酱香猪蹄500g', 19, '卤制猪蹄，软糯脱骨，开袋即食', 100, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (68, '香辣藕片150g', 19, '卤味藕片，脆爽入味，休闲零食', 250, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (69, '纯棉四件套1.8m', 20, '全棉四件套，亲肤透气，花色简约', 80, 199.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (70, '乳胶枕成人款', 20, '天然乳胶枕，护颈助眠，透气防螨', 100, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (71, '法兰绒毛毯1.5m', 20, '加厚法兰绒毯，保暖亲肤，四季可用', 120, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (72, '防水床笠1.2m', 20, '隔尿床笠，透气不闷，防滑固定', 150, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (73, '原味蛋挞皮30个', 21, '半成品蛋挞皮，酥皮起酥，家用烘焙', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (74, '高筋面粉1kg', 21, '面包专用高筋粉，筋度高，易出膜', 180, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (75, '淡奶油250ml', 21, '动物淡奶油，易打发，裱花烘焙专用', 150, 18.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (76, '医用口罩一次性50只', 22, '三层防护口罩，熔喷布过滤，透气', 500, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (77, '布洛芬缓释胶囊24粒', 22, '缓解头痛关节痛，退烧止痛', 300, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (78, '医用酒精75%500ml', 22, '消毒酒精，皮肤物品消毒，杀菌', 250, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (79, '创可贴防水100片', 22, '防水创可贴，透气不闷，止血护创', 400, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (80, '汽车玻璃水-25℃', 23, '防冻玻璃水，去污去油膜，冬季专用', 200, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (81, '车载手机支架', 23, '出风口支架，防抖稳固，单手操作', 250, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (82, '汽车座套四季通用', 23, '全包座套，透气耐磨，贴合座椅', 80, 199.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (83, '剁椒鱼头调料包', 24, '正宗剁椒调料，酸辣入味，一料成菜', 200, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (84, '湖南腊肉500g', 24, '农家烟熏腊肉，肥瘦相间，炒菜香', 100, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (85, '外婆菜200g', 24, '下饭外婆菜，咸香开胃，炒肉绝配', 250, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (86, '干辣椒粉200g', 24, '湖南辣椒粉，香辣够味，炒菜调味', 180, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (87, '瓶装矿泉水550ml*12', 25, '天然矿泉水，解渴饮用，整箱更划算', 150, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (88, '巧克力派10枚', 25, '夹心巧克力派，早餐代餐，休闲零食', 200, 18.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (89, '桶装方便面单桶', 25, '多口味泡面，速食方便，开水冲泡', 300, 4.50, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (90, '厨房置物架落地', 26, '多层置物架，收纳锅具，节省空间', 100, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (91, '免打孔挂钩5个', 26, '强力粘钩，承重力强，免钉安装', 300, 6.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (92, '不锈钢菜盆沥水篮', 26, '双层沥水篮，洗菜洗水果，实用方便', 200, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (93, '硅胶隔热垫6片', 26, '防烫隔热垫，耐高温，防滑耐磨', 250, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (94, '安踏运动鞋男款', 27, '网面透气运动鞋，轻便减震，防滑鞋底', 80, 299.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (95, '速干运动T恤女', 27, '速干面料T恤，吸汗透气，运动专用', 100, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (96, '运动短裤男速干', 27, '速干短裤，宽松版型，跑步健身均可', 120, 69.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (97, '芦荟胶300g', 28, '补水芦荟胶，晒后修复，舒缓保湿', 200, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (98, '烟酰胺精华液30ml', 28, '提亮精华，改善暗沉，淡化痘印', 150, 99.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (99, '身体乳保湿500g', 28, '全身保湿身体乳，滋润不油腻，留香久', 180, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (100, '去角质啫喱150g', 28, '温和去角质，深层清洁，提亮肤色', 160, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (101, '得力打印纸A4 500张', 29, '70gA4打印纸，不卡纸，字迹清晰', 100, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (102, '中性笔芯0.5mm 20支', 29, '通用笔芯，书写顺滑，速干不脏手', 300, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (103, '档案盒A4塑料', 29, '加厚档案盒，收纳文件，耐用防潮', 200, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (104, '百香果果酱1kg', 30, '浓缩果酱，冲调饮品，果味浓郁', 150, 25.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (105, '气泡水机专用气瓶', 30, '食品级气瓶，充气方便，自制气泡水', 80, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (106, '速溶咖啡三合一', 30, '原味速溶咖啡，提神醒脑，冲调方便', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (107, '椰果粒500g', 30, '奶茶专用椰果，Q弹爽口，多种口味', 180, 10.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (108, '老北京炸酱面酱料', 31, '正宗炸酱面酱料，酱香浓郁，拌面专用', 200, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (109, '手工拉面500g', 31, '鲜拉面，劲道爽滑，煮制不坨', 180, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (110, '凉拌面调料包', 31, '麻酱凉拌面调料，酸甜适口，方便快捷', 250, 6.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (111, '酸奶水果捞成品300g', 32, '现做水果捞，多种水果，酸奶浓郁', 100, 18.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (112, '芒果丁100g', 32, '新鲜芒果丁，冷冻锁鲜，水果捞专用', 150, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (113, '椰果水果捞配料', 32, '彩色椰果，Q弹爽口，增加口感', 200, 5.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (114, '奥利奥碎50g', 32, '饼干碎，水果捞/甜品装饰，口感丰富', 180, 4.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (115, '婴幼儿奶粉1段800g', 33, '0-6个月婴儿奶粉，营养均衡，易吸收', 80, 299.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (116, '孕产妇奶粉900g', 33, '孕期哺乳期奶粉，补充叶酸钙铁', 100, 199.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (117, '儿童成长奶粉4段', 33, '3-6岁儿童奶粉，高钙补锌，助力成长', 120, 259.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (118, '垃圾桶家用带盖', 34, '按压式垃圾桶，防异味，客厅厨房通用', 200, 25.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (119, '洗碗布不沾油10条', 34, '竹纤维洗碗布，吸水吸油，易清洗', 300, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (120, '衣架防滑无痕10个', 34, '塑料衣架，防滑无痕，承重力强', 250, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (121, '粘毛器可撕式', 34, '滚筒粘毛器，除毛除尘，替换装方便', 200, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (122, '牛油火锅底料特辣', 35, '正宗重庆火锅底料，麻辣鲜香，越煮越香', 200, 18.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (123, '火锅肥牛卷200g', 35, '新鲜肥牛卷，肥瘦相间，涮锅必备', 150, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (124, '火锅川粉宽粉', 35, '红薯宽粉，耐煮不烂，吸满汤汁', 250, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (125, '纯牛奶250ml*16盒', 36, '全脂纯牛奶，营养早餐，整箱更划算', 150, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (126, '鸡蛋40枚装', 36, '农家散养土鸡蛋，新鲜无抗生素', 200, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (127, '金龙鱼食用油5L', 36, '非转基因大豆油，家用烹饪，油烟少', 100, 69.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (128, '速冻水饺1kg', 36, '三鲜水饺，皮薄馅大，冷冻速食', 180, 25.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (129, '黄焖鸡米饭料理包', 37, '加热即食料理包，鸡肉鲜嫩，汤汁浓郁', 150, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (130, '台式烤肠500g', 37, '脆皮烤肠，烧烤煎炸，早餐快餐专用', 200, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (131, '酸辣土豆丝料理包', 37, '速食料理包，酸辣开胃，配饭神器', 250, 6.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (132, '手机充电宝20000mAh', 38, '大容量充电宝，双向快充，便携耐用', 120, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (133, '数据线快充2米', 38, '加长快充线，耐弯折，兼容多机型', 250, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (134, '手机壳防摔全包', 38, '硅胶手机壳，防摔抗震，多色可选', 300, 25.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (135, '蓝牙耳机套硅胶', 38, '防摔耳机套，防尘防水，卡通款式', 200, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (136, '高腰牛仔短裙', 39, '显瘦牛仔短裙，A字版型，百搭款', 100, 79.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (137, '冰丝防晒衣女', 39, '超薄防晒衣，透气防晒，夏季必备', 150, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (138, '针织吊带背心', 39, '百搭吊带，内搭外穿均可，多色可选', 200, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (139, '铁观音茶叶250g', 40, '新茶铁观音，清香型，耐泡回甘', 100, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (140, '功夫茶具套装', 40, '陶瓷茶具，简约大方，家用待客', 80, 129.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (141, '小青柑普洱茶', 40, '新会小青柑，果香普洱，一颗一泡', 90, 69.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (142, '茶盘实木家用', 40, '排水茶盘，实木材质，耐用易清洁', 70, 99.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (143, '冷冻巴沙鱼柳1kg', 41, '无骨无刺巴沙鱼，鲜嫩少腥，宝宝辅食', 150, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (144, '花甲500g', 41, '鲜活花甲，无沙，爆炒香辣超入味', 200, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (145, '八爪鱼300g', 41, '迷你八爪鱼，脆嫩Q弹，火锅烧烤食材', 120, 28.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (146, '五香牛肉200g', 42, '卤制牛肉，肉质紧实，开袋即食', 150, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (147, '卤鸭翅180g', 42, '香辣鸭翅，追剧零食，真空包装', 200, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (148, '泡椒凤爪200g', 42, '酸辣凤爪，脱骨入味，休闲小吃', 180, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (149, '卤豆干150g', 42, '麻辣豆干，Q弹入味，素食零食', 250, 7.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (150, '蚕丝被春秋被', 43, '天然蚕丝被，透气保暖，四季通用', 60, 399.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (151, '枕头一对装', 43, '高弹枕头，护颈舒适，家用酒店款', 100, 59.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (152, '珊瑚绒毛巾被', 43, '薄款毛巾被，透气亲肤，夏季盖毯', 120, 79.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (153, '可可粉烘焙专用', 44, '无糖可可粉，提拉米苏/蛋糕专用', 180, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (154, '裱花袋裱花嘴套装', 44, '一次性裱花袋，多款式裱花嘴', 200, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (155, '面包糠油炸专用', 44, '金黄色面包糠，炸鸡炸虾裹粉', 250, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (156, '吉利丁片10片', 44, '食用吉利丁，做果冻布丁专用', 150, 9.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (157, '板蓝根颗粒10袋', 45, '清热解毒，缓解感冒咽喉痛', 300, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (158, '云南白药创可贴', 45, '止血镇痛，消炎愈创，轻巧护创', 400, 10.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (159, '健胃消食片60片', 45, '开胃消食，缓解腹胀消化不良', 250, 8.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (160, '汽车机油5W-40 4L', 46, '全合成机油，润滑保护发动机', 80, 199.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (161, '空气滤芯通用款', 46, '过滤灰尘，保护发动机，易更换', 150, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (162, '刹车油DOT4 1L', 46, '制动液，耐高温，刹车更灵敏', 100, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (163, '雨刮器通用款', 46, '无骨雨刮器，静音刮水，高清视野', 200, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (164, '港式奶茶粉500g', 47, '正宗港式奶茶，丝滑浓郁，茶味重', 150, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (165, '菠萝包半成品', 47, '冷冻菠萝包，加热即食，酥皮香甜', 200, 18.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (166, '港式叉烧酱250g', 47, '腌制叉烧专用酱，甜咸适口', 180, 15.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (167, '红牛饮料250ml*6', 48, '能量饮料，提神醒脑，运动加班必备', 150, 39.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (168, '薯片超大包分享装', 48, '混合口味薯片，聚会分享，休闲零食', 200, 19.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (169, '火腿肠整箱50根', 48, '即食火腿肠，煎炸烧烤均可', 180, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (170, '口香糖薄荷味40粒', 48, '清新口气，无糖配方，便携装', 250, 12.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (171, '东北黑木耳250g', 49, '椴木黑木耳，肉厚无根，泡发率高', 150, 29.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (172, '榛蘑干货200g', 49, '东北榛蘑，小鸡炖蘑菇专用，鲜香', 120, 35.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (173, '松子原味500g', 49, '东北开口松子，颗粒饱满，坚果零食', 100, 89.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (174, '李宁运动跑鞋男', 50, '减震跑鞋，网面透气，轻便防滑', 80, 399.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (175, '运动紧身裤女', 50, '高弹紧身裤，速干透气，健身瑜伽', 100, 99.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (176, '羽毛球拍单拍', 50, '碳素羽毛球拍，轻便耐打，攻防兼备', 70, 199.00, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');
INSERT INTO `goods` VALUES (177, '运动水壶大容量', 50, '食品级运动水壶，防漏便携，保温保冷', 149, 49.90, 'https://img.shetu66.com/2023/07/06/1688611155457376.png');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `OrderID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NULL DEFAULT NULL,
  `StoreID` int NULL DEFAULT NULL,
  `GoodsID` int NULL DEFAULT NULL,
  `Count` int NULL DEFAULT NULL,
  `Price` decimal(10, 2) NULL DEFAULT NULL,
  `Time` datetime NULL DEFAULT NULL,
  `State` int NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`OrderID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (42, 14, 1, 4, 100, 550.00, '2026-01-23 08:56:20', 4, '家');
INSERT INTO `orders` VALUES (43, 16, 1, 1, 10, 25.00, '2026-01-23 09:04:52', 1, '我家');
INSERT INTO `orders` VALUES (44, 16, 2, 7, 8, 103.20, '2026-01-23 09:04:52', 3, '我家');
INSERT INTO `orders` VALUES (45, 16, 1, 4, 10, 55.00, '2026-01-23 09:27:21', 0, '我家');
INSERT INTO `orders` VALUES (46, 16, 2, 6, 1, 25.80, '2026-01-23 09:27:22', 0, '我家');
INSERT INTO `orders` VALUES (47, 16, 2, 7, 5, 64.50, '2026-01-23 09:27:22', 0, '我家');
INSERT INTO `orders` VALUES (48, 16, 2, 8, 5, 99.50, '2026-01-23 09:27:22', 0, '我家');
INSERT INTO `orders` VALUES (49, 14, 50, 177, 1, 49.90, '2026-01-23 22:56:19', 1, '家');

-- ----------------------------
-- Table structure for shoppingcart
-- ----------------------------
DROP TABLE IF EXISTS `shoppingcart`;
CREATE TABLE `shoppingcart`  (
  `ShoppingCartID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `StoreID` int NOT NULL,
  `GoodsID` int NOT NULL,
  `Count` int NULL DEFAULT NULL,
  `TotalPrice` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`ShoppingCartID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shoppingcart
-- ----------------------------

-- ----------------------------
-- Table structure for store
-- ----------------------------
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store`  (
  `StoreID` int NOT NULL AUTO_INCREMENT,
  `StoreName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `StoreEmail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `Picture` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`StoreID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of store
-- ----------------------------
INSERT INTO `store` VALUES (1, '晨光文具旗舰店', 'merchant1@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (2, '五谷杂粮旗舰店', 'merchant2@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (3, '每日鲜生鲜旗舰店', 'merchant3@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (4, '潮流男装专营店', 'merchant4@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (5, '丽人美妆官方店', 'merchant5@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (6, '悦家生活旗舰店', 'merchant6@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (7, '甜趣茶饮专营店', 'merchant7@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (8, '好食光食品专营店', 'merchant8@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (9, '果缤纷水果旗舰店', 'merchant9@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (10, '贝贝母婴官方店', 'merchant10@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (11, '五金建材专营店', 'merchant11@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (12, '川味美食旗舰店', 'merchant12@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (13, '乐家生活超市店', 'merchant13@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (14, '速食客快餐专营店', 'merchant14@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (15, '酷玩数码旗舰店', 'merchant15@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (16, '衣品女装专营店', 'merchant16@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (17, '益杯茶官方旗舰店', 'merchant17@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (18, '鲜海汇水产专营店', 'merchant18@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (19, '卤味世家旗舰店', 'merchant19@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (20, '爱家家纺专营店', 'merchant20@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (21, '麦香烘焙旗舰店', 'merchant21@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (22, '康健大药房官方店', 'merchant22@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (23, '爱车族汽车用品店', 'merchant23@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (24, '湘味小厨专营店', 'merchant24@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (25, '蜂享家便利店', 'merchant25@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (26, '惠享优选专营店', 'merchant26@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (27, '安踏运动专营店', 'merchant27@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (28, '自然美护肤旗舰店', 'merchant28@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (29, '得力办公专营店', 'merchant29@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (30, '蜜甜饮品旗舰店', 'merchant30@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (31, '京味面道专营店', 'merchant31@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (32, '果鲜生水果捞店', 'merchant32@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (33, '优哺奶粉专营店', 'merchant33@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (34, '日用百货旗舰店', 'merchant34@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (35, '渝味火锅专营店', 'merchant35@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (36, '鲜生优选超市店', 'merchant36@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (37, '速享客快餐旗舰店', 'merchant37@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (38, '华讯数码专营店', 'merchant38@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (39, '秋水衣坊女装店', 'merchant39@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (40, '茶语时光旗舰店', 'merchant40@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (41, '海之鲜水产专营店', 'merchant41@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (42, '卤香阁官方店', 'merchant42@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (43, '罗莱雅家纺旗舰店', 'merchant43@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (44, '焙甜时光烘焙店', 'merchant44@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (45, '本草堂大药房', 'merchant45@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (46, '车管家维修专营店', 'merchant46@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (47, '粤港茶餐厅旗舰店', 'merchant47@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (48, '7客便利店专营店', 'merchant48@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (49, '东北特产官方店', 'merchant49@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (50, '李宁运动旗舰店', 'merchant50@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');
INSERT INTO `store` VALUES (51, '喜欢供应链', 'merchant51@qq.com', 'https://pic.616pic.com/ys_bnew_img/00/37/57/sEotdoQgg5.jpg');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `UserName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `UserPwd` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `UserEmail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `UserPicture` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`UserID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;


SET FOREIGN_KEY_CHECKS = 1;
