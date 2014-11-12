-- 初始化sql
create table reqs(
  rec_id int(11) primary key auto_increment,
  from_ip varchar(50),
  url varchar(3000) comment '实际要请求的url',
  header text comment '请求的header',
  body varchar(12000) comment '请求体',
  response text comment '响应结果',
  status int(10) comment '请求的http状态',
  pre_status int(10) comment '前一次请求的状态,如果失败,日终执行',
  invoke_by int(5) comment '执行请求者! 0为系统执行 1为直接请求',
  add_time datetime,
  update_time datetime
);

create index idx_url_body on reqs(url,body);
create index idx_pre_status on reqs(pre_status);