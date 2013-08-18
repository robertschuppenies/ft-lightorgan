% ftlo_pipe_map.m
% 
% this is a script to map "pitch" bins into the pipe locations going from 
% longest to shortest (interleaving left and right).
%
% Luke Dahl, 8/17/13


Npt = 51; % The number of pitch bins
Npp = 51; % The number of pipes

n_front = 26;   % number of pipes in front row
n_rear = 25;    % number of pipes in back row

lowest = n_front + floor(n_rear/2);  % should be 38

map_array = zeros(Npp,1);

% index is pitch bin, output is pipe number

% back row
map_array(1) = lowest;
map_array(2:2:24) = lowest*ones(1,12) - (1:12);
map_array(3:2:25) = lowest*ones(1,12) + (1:12);

% front row
mid_front = n_front/2;
map_array(26:2:51) = mid_front*ones(1,13) - (1:13);
map_array(27:2:51) = mid_front*ones(1,13) + (0:12);



%% write map array into a text file
fName = 'pitch_to_pipe_map.txt';
fid = fopen(fName,'w');            %# Open the file
if fid == -1
  disp('error opening file');
  fclose(fid);                     %# Close the file
end

fprintf(fid, ' [ ');
for i = 1:Npt
    fprintf( fid, '%d, ',map_array(i));
    % fprintf( fid, ', ');
end
fprintf(fid, '];\n');
fclose(fid);