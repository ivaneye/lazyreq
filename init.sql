-- 初始化sql
create table reqs(
  rec_id int(11) primary key auto_increment,
  from_ip varchar(50),
  url text comment '实际要请求的url',
  url_md5 varchar(100) comment '加密后的url,作为索引使用',
  header text comment '请求的header',
  body text comment '请求体',
  body_md5 varchar(100) comment '加密后的body,作为索引使用',
  response text comment '响应结果',
  status int(10) comment '请求的http状态',
  pre_status int(10) comment '前一次请求的状态,如果失败,日终执行',
  invoke_by int(5) comment '执行请求者! 0为系统执行 1为直接请求',
  add_time datetime,
  update_time datetime
);

create index idx_url_body on reqs(url_md5,body_md5);
create index idx_pre_status on reqs(pre_status);