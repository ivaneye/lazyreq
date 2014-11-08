create table reqs(
  rec_id int(11) primary key auto_increment,
  from_ip varchar(50),
  url text comment '实际要请求的url',
  header text comment '请求的header',
  body text comment '请求体',
  response text comment '响应结果',
  status int(10) comment '请求的http状态',
  invoke_by int(5) comment '执行请求者! 0为系统执行 1为直接请求',
  add_time datetime,
  update_time datetime
);