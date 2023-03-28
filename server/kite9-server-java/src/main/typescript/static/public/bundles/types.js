export const directions = ["up", "down", "left", "right"];
export function rotateClockwise(d) {
    switch (d) {
        case 'up':
            return 'right';
        case 'down':
            return 'left';
        case 'left':
            return 'up';
        case 'right':
            return 'down';
        case undefined:
            return undefined;
    }
}
export function rotateAntiClockwise(d) {
    switch (d) {
        case 'up':
            return 'left';
        case 'down':
            return 'right';
        case 'left':
            return 'down';
        case 'right':
            return 'up';
        case undefined:
            return undefined;
    }
}
export function reverseDirection(d) {
    switch (d) {
        case "left":
            return "right";
        case "up":
            return "down";
        case "down":
            return "up";
        case "right":
            return "left";
        case undefined:
            return undefined;
    }
}
export function sharedArea(a1, a2) {
    const x1 = Math.max(a1.x, a2.x);
    const y1 = Math.max(a1.y, a2.y);
    const x2 = Math.min(a1.x + a1.width, a2.x + a2.width);
    const y2 = Math.min(a1.y + a1.height, a2.y + a2.height);
    return {
        x: x1,
        y: y1,
        width: Math.max(0, x2 - x1),
        height: Math.max(0, y2 - y1)
    };
}
export function intersects(r1, r2) {
    const startIn = (r1[0] >= r2[0]) && (r1[0] < r2[1]);
    const endIn = (r1[1] > r2[0]) && (r1[1] <= r2[1]);
    return startIn || endIn;
}
