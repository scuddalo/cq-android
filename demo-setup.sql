insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (8, 1, 3, now(), now(), 1); --santoash follows tester
insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (8, 4, 3, now(), now(), 1); --santoash follows karan
insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (8, 5, 1, now(), now(), 1); --santoash follows sunny
insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (8, 6, 3, now(), now(), 1); --santoash follows chris

insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (1, 2, 3, now(), now(), 1);  --tester follows tester2
insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (1, 8, 3, now(), now(), 1); --tester follows santoash
insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (1, 10, 3, now(), now(), 1);  --tester follows tom cruise
insert into friendships (follower_id, followee_id, tier, created_at, updated_at, status) values (1, 9, 3, now(), now(), 1);  --tester follows jackie chan




--locations setup 

insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.771682, -122.28017, now(), now());
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.77370, -122.27659, now(), now());
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.77363, -122.29023, now(), now());
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.77482, -122.28118, now(), now());
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.77680, -122.27753, now(), now());
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.769171, -122.27839, now(), now());

insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.80904, -122.41027, now(), now()); 
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.77825, -122.38924, now(), now()); 
insert into locations (address, location_type, latitude, longitude, created_at, updated_at) values (null, 'Address', 37.77641, -122.39448, now(), now()); 



update profiles set location_id=2 where id=8;
update profiles set location_id=8 where id=5;
update profiles set location_id=3 where id=4;
update profiles set location_id=4 where id=6;
	
update profiles set location_id=5 where id=2;
update profiles set location_id=7 where id=10;
update profiles set location_id=9 where id=9;