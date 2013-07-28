% ftlo_log_freq.m
% 
% this is a script to group fft bins (spaced linearly in frequency) into a
% smaller number of logarithmically spaced "pitch" bins.
%
% Luke Dahl, 7/27/13

%%
fs = 44100; % the audio sample rate
Nf = 1024;   % the number of fft bins
Np = 25;    % the number of pitch bins

% figure out which fft bin is the lowest we'll use (at 20Hz)
n_lo = ceil(2*20*Nf/fs);

% a logarithmic spacing
y = logspace(log10(n_lo),log10(Nf),Np);
y = ceil(y);   % y is the top freq bin for each pitch bin

% make a mapping
map_array = zeros(1, Np);
ind_f = 1:Nf;

for i=Np:-1:1
    ix_temp = find(ind_f <= y(i));
    map_array(ix_temp) = i;
end

%% write map array into a text file
fName = 'freq_to_pitch_map.txt';
fid = fopen(fName,'w');            %# Open the file
if fid == -1
  disp('error opening file');
  fclose(fid);                     %# Close the file
end

fprintf(fid, ' [ ');
for i = 1:Nf
    fprintf( fid, '%d, ',map_array(i)-1);
    % fprintf( fid, ', ');
end
fprintf(fid, '];\n');
fclose(fid);