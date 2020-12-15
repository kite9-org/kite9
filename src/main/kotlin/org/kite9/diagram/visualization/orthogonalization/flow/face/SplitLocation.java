package org.kite9.diagram.visualization.orthogonalization.flow.face;

import org.kite9.diagram.common.algorithms.fg.Node;

class SplitLocation {

    Node split;
    Object location;

    public SplitLocation(Node split, Object location) {
        this.split = split;
        this.location = location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((split == null) ? 0 : split.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SplitLocation other = (SplitLocation) obj;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (split == null) {
            if (other.split != null)
                return false;
        } else if (!split.equals(other.split))
            return false;
        return true;
    }


}
