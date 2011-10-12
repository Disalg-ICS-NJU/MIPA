function createfigure(x1, y1)
%CREATEFIGURE(X1,Y1)
%  X1:  stem x
%  Y1:  stem y

%  Auto-generated by MATLAB on 12-Aug-2011 23:44:01

% Create figure
figure1 = figure;
colormap('lines');

% Create axes
axes1 = axes('Parent',figure1,'YGrid','on',...
    'XTick',[0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20],...
    'FontSize',12);
% Uncomment the following line to preserve the X-limits of the axes
% xlim(axes1,[0 19]);
% Uncomment the following line to preserve the Y-limits of the axes
% ylim(axes1,[0 50]);
hold(axes1,'all');

% Create xlabel
xlabel('n-th Experiment','Interpreter','latex','LineWidth',1,...
    'FontWeight','bold',...
    'FontSize',14,...
    'FontName','Microsoft YaHei');

% Create ylabel
ylabel('Number of Checks with ``inconclusive'''' ($NCheck_{inc}$)',...
    'Interpreter','latex',...
    'FontWeight','bold',...
    'FontSize',12,...
    'FontName','Microsoft YaHei');

% Create title
title('Occurrences of the third value ``inconclusive''''',...
    'Interpreter','latex',...
    'FontSize',20,...
    'FontName','Microsoft YaHei');

% Create stem
stem1 = stem(x1,y1,'LineWidth',1,'Color',[0 0 1],'Parent',axes1,...
    'DisplayName','   number of checks');

% Get xdata from plot
xdata1 = get(stem1, 'xdata');
% Get ydata from plot
ydata1 = get(stem1, 'ydata');
% Make sure data are column vectors
xdata1 = xdata1(:);
ydata1 = ydata1(:);

% Get axes xlim
axXLim1 = get(axes1, 'xlim');

% Find the mean
ymean1 = mean(ydata1);
% Get coordinates for the mean line
meanValue1 = [ymean1 ymean1];
% Plot the mean
statLine1 = plot(axXLim1,meanValue1,'DisplayName','   y mean',...
    'Parent',axes1,...
    'Tag','mean y',...
    'LineStyle','-.',...
    'Color',[0 0.5 0]);

% Set new line in proper position
setLineOrder(axes1, statLine1, stem1);

% Create legend
legend(axes1,'show');

%-------------------------------------------------------------------------%
function setLineOrder(axesh1, newLine1, associatedLine1)
%SETLINEORDER(AXESH1,NEWLINE1,ASSOCIATEDLINE1)
%  Set line order
%  AXESH1:  axes
%  NEWLINE1:  new line
%  ASSOCIATEDLINE1:  associated line

% Get the axes children
hChildren = get(axesh1,'Children');
% Remove the new line
hChildren(hChildren==newLine1) = [];
% Get the index to the associatedLine
lineIndex = find(hChildren==associatedLine1);
% Reorder lines so the new line appears with associated data
hNewChildren = [hChildren(1:lineIndex-1);newLine1;hChildren(lineIndex:end)];
% Set the children:
set(axesh1,'Children',hNewChildren);
