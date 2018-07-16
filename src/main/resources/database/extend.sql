USE rap_db;
-- 功能扩展sql


-- 扩展接口返回状态码信息
CREATE TABLE `tb_return_code_list_mapping` (
  `action_id` int(10) NOT NULL,
  `parameter_id` int(10) NOT NULL,
  PRIMARY KEY (`action_id`,`parameter_id`),
  KEY `tb_return_code_list_mapping_fk2` (`parameter_id`),
  CONSTRAINT `tb_return_code_list_mapping_fk1` FOREIGN KEY (`action_id`) REFERENCES `tb_action` (`id`),
  CONSTRAINT `tb_return_code_list_mapping_fk2` FOREIGN KEY (`parameter_id`) REFERENCES `tb_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8


-- 根据产品线展示主页数据（在project添加产品线）
ALTER TABLE tb_project
ADD COLUMN corporation_id TINYINT NOT NULL
DEFAULT 0;


-- 添加用户逻辑删除状态字段
ALTER TABLE tb_user
ADD COLUMN deleted TINYINT NOT NULL
DEFAULT 0;
