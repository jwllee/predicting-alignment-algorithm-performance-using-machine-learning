#!/usr/bin/env python


import podspy
import podspy.petrinet
import numpy as np
import functools as fct
import collections as cols


# net features
N_TRAN = 'n_transition'
N_INV_TRAN = 'n_inv_transition'
N_DUP_TRAN = 'n_dup_transition'
N_UNIQ_TRAN = 'n_uniq_transition'
INV_TRAN_MEAN_IN_DEG = 'inv_transition_mean_in_degree'
INV_TRAN_STD_IN_DEG = 'inv_transition_std_in_degree'
INV_TRAN_MEAN_OUT_DEG = 'inv_transition_mean_out_degree'
INV_TRAN_STD_OUT_DEG = 'inv_transition_std_out_degree'
UNIQ_TRAN_MEAN_IN_DEG = 'uniq_transition_mean_in_degree'
UNIQ_TRAN_STD_IN_DEG = 'uniq_transition_std_in_degree'
UNIQ_TRAN_MEAN_OUT_DEG = 'uniq_transition_mean_out_degree'
UNIQ_TRAN_STD_OUT_DEG = 'uniq_transition_std_out_degree'
DUP_TRAN_MEAN_IN_DEG = 'dup_transition_mean_in_degree'
DUP_TRAN_STD_IN_DEG = 'dup_transition_std_in_degree'
DUP_TRAN_MEAN_OUT_DEG = 'dup_transition_mean_out_degree'
DUP_TRAN_STD_OUT_DEG = 'dup_transition_std_out_degree'
PLACE_MEAN_IN_DEG = 'place_mean_in_deg'
PLACE_STD_IN_DEG = 'place_std_in_deg'
PLACE_MEAN_OUT_DEG = 'place_mean_out_deg'
PLACE_STD_OUT_DEG = 'place_std_out_deg'
N_PLACE = 'n_place'
N_ARC = 'n_arc'
N_AND_SPLIT = 'n_and_split'
N_XOR_SPLIT = 'n_xor_split'
N_STRONG_COMPONENT = 'n_strong_component'
N_BICONNECTED_COMPONENT = 'n_biconnected'

# decomposition related
N_SUBNET = 'n_subnet'                             # for monolithic it's just 1
SUBNET_MEAN_N_TRAN = 'subnet_mean_n_transition'   # for monolithic just N_TRAN
SUBNET_STD_N_TRAN = 'subnet_std_n_transition'     # for monolithic it's just 1


def extract_features(pn):
    """Extract required features given a petri net

    :param pn:
    :return:
    """
    pass


def get_n_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return len(pn.transitions)


def get_dup_tran_dict(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    tran_dict = cols.defaultdict(list)
    for t in pn.transitions:
        if t.is_invisible:
            continue
        tran_dict[t.label].append(t)
    to_remove = [t_label for t_label in tran_dict.keys() if len(tran_dict[t_label]) == 1]
    for t_label in to_remove:
        del tran_dict[t_label]
    return dict(tran_dict)


def get_dup_tran_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    dup_tran = get_dup_tran_dict(pn)
    return fct.reduce(lambda _all, tran_list: _all + tran_list, dup_tran, [])


def get_uniq_tran_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    tran_dict = cols.defaultdict(list)
    for t in pn.transitions:
        if t.is_invisible:
            continue
        tran_dict[t.label].append(t)
    uniq_tran = [tran_dict[t_label] for t_label in tran_dict.keys() if len(tran_dict[t_label]) == 1]
    return fct.reduce(lambda _all, tran_set: _all + list(tran_set), uniq_tran, [])


def get_inv_tran_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return list(filter(lambda t: t.is_invisible, pn.transitions))


def get_n_inv_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return len(get_inv_tran_list(pn))


def get_n_dup_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return len(list(get_dup_tran_dict(pn).keys()))


def get_n_uniq_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return len(get_uniq_tran_list(pn))


def get_n_place(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return len(pn.places)


def get_n_arc(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    return len(pn.arcs)


def get_n_and_split(pn):
    """Get the number of AND split of a petrinet. It is an AND split if a transition has more than one outgoing arc.

    :param pn: Petrinet
    :return: number of AND split
    """
    assert isinstance(pn, podspy.petrinet.Petrinet)
    adj_list = {}
    for t in pn.transitions:
        t_edges = pn.get_directed_edges(src=t)
        adj_list[t] = len(t_edges)
    and_split = filter(lambda t: adj_list[t] > 1, adj_list.keys())
    return len(list(and_split))


def get_n_xor_split(pn):
    """Get the number of XOR split of a petrinet. It is an XOR split if a place has more than one outgoing arc.

    :param pn: Petrinet
    :return: number of XOR split
    """
    assert isinstance(pn, podspy.petrinet.Petrinet)
    adj_list = {}
    for p in pn.places:
        p_edges = pn.get_directed_edges(src=p)
        adj_list[p] = len(p_edges)
    xor_split = filter(lambda p: adj_list[p] > 1, adj_list.keys())
    return len(list(xor_split))


def get_inv_tran_in_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    inv_tran = get_inv_tran_list(pn)
    in_deg_list = []
    for t in inv_tran:
        t_edges = pn.get_directed_edges(target=t)
        in_deg_list.append(len(t_edges))
    return in_deg_list


def get_inv_tran_out_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    inv_tran = get_inv_tran_list(pn)
    out_deg_list = []
    for t in inv_tran:
        t_edges = pn.get_directed_edges(src=t)
        out_deg_list.append(len(t_edges))
    return out_deg_list


def get_inv_tran_mean_in_deg(pn):
    in_deg_list = get_inv_tran_in_deg_list(pn)
    return np.mean(in_deg_list) if in_deg_list else 0.


def get_inv_tran_std_in_deg(pn):
    in_deg_list = get_inv_tran_in_deg_list(pn)
    return np.std(in_deg_list) if in_deg_list else 0.


def get_inv_tran_mean_out_deg(pn):
    out_deg_list = get_inv_tran_out_deg_list(pn)
    return np.mean(out_deg_list) if out_deg_list else 0.


def get_inv_tran_std_out_deg(pn):
    out_deg_list = get_inv_tran_out_deg_list(pn)
    return np.std(out_deg_list) if out_deg_list else 0.


def get_uniq_tran_in_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    uniq_tran = get_uniq_tran_list(pn)
    in_deg_list = []
    for t in uniq_tran:
        t_edges = pn.get_directed_edges(target=t)
        in_deg_list.append(len(t_edges))
    return in_deg_list


def get_uniq_tran_out_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    uniq_tran = get_uniq_tran_list(pn)
    out_deg_list = []
    for t in uniq_tran:
        t_edges = pn.get_directed_edges(src=t)
        out_deg_list.append(len(t_edges))
    return out_deg_list


def get_uniq_tran_mean_in_deg(pn):
    in_deg_list = get_uniq_tran_in_deg_list(pn)
    return np.mean(in_deg_list) if in_deg_list else 0.


def get_uniq_tran_std_in_deg(pn):
    in_deg_list = get_uniq_tran_in_deg_list(pn)
    return np.std(in_deg_list) if in_deg_list else 0.


def get_uniq_tran_mean_out_deg(pn):
    return np.mean(get_uniq_tran_out_deg_list(pn))


def get_uniq_tran_std_out_deg(pn):
    return np.mean(get_uniq_tran_out_deg_list(pn))


def get_dup_tran_in_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    dup_tran = get_dup_tran_list(pn)
    in_deg_list = []
    for t in dup_tran:
        t_edges = pn.get_directed_edges(target=t)
        in_deg_list.append(len(t_edges))
    return in_deg_list


def get_dup_tran_out_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    dup_tran = get_dup_tran_list(pn)
    out_deg_list = []
    for t in dup_tran:
        t_edges = pn.get_directed_edges(src=t)
        out_deg_list.append(len(t_edges))
    return out_deg_list


def get_dup_tran_mean_in_deg(pn):
    return np.mean(get_dup_tran_in_deg_list(pn))


def get_dup_tran_std_in_deg(pn):
    return np.std(get_dup_tran_in_deg_list(pn))


def get_dup_tran_mean_out_deg(pn):
    return np.mean(get_uniq_tran_out_deg_list(pn))


def get_dup_tran_std_out_deg(pn):
    return np.std(get_dup_tran_out_deg_list(pn))


def get_place_in_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    in_deg_list = []
    for p in pn.places:
        p_edges = pn.get_directed_edges(target=p)
        in_deg_list.append(len(p_edges))
    return in_deg_list


def get_place_out_deg_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    out_deg_list = []
    for p in pn.places:
        p_edges = pn.get_directed_edges(src=p)
        out_deg_list.append(len(p_edges))
    return out_deg_list


def pn_to_undirected(pn):
    # return as adjacency list
    adj = cols.defaultdict(list)

    for node in pn.get_nodes():
        for edge in pn.get_directed_edges(src=node):
            adj[node].append(edge.target)
            adj[edge.target].append(node)

    return dict(adj)


def get_biconnected_component_list(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    components = list()

    G = pn_to_undirected(pn)

    # print('Graph: ')
    #
    # for node in G.keys():
    #     for v in G[node]:
    #         print('{}-{}'.format(node.label, v.label), end=' ')
    #     print()
    #
    # print()

    visited = set()

    for start in G.keys():
        if start in visited:
            continue

        # print('{} as root'.format(start.label))

        discovery = {start:0}
        low = {start:0}
        root_children = 0
        visited.add(start)
        edge_stack = []
        stack = list()
        stack.append((start, start, iter(G[start])))

        while stack:
            grandparent, parent, children = stack[-1]
            try:
                child = next(children)
                if grandparent == child:
                    # print('Skip {}-{}'.format(parent.label, child.label))
                    continue
                # print('Visit {}-{}'.format(parent.label, child.label))
                if child in visited:
                    if discovery[child] <= discovery[parent]:   # back edge
                        low[parent] = min(low[parent], discovery[child])
                        edge_stack.append((parent, child))
                else:
                    low[child] = discovery[child] = len(discovery)
                    visited.add(child)
                    stack.append((parent, child, iter(G[child])))
                    edge_stack.append((parent, child))
            except StopIteration:
                stack.pop()
                # print('Stopped at {} {}'.format(grandparent.label, parent.label))
                if len(stack) > 1:
                    if low[parent] >= discovery[grandparent]:
                        # print('Bi: {}-{}'.format(grandparent.label, parent.label))
                        # grandparent is an articulation point
                        ind = edge_stack.index((grandparent, parent))
                        components.append(edge_stack[ind:])
                        edge_stack = edge_stack[:ind]
                    low[grandparent] = min(low[parent], low[grandparent])
                elif stack: # grandparent is root
                    root_children += 1
                    ind = edge_stack.index((grandparent, parent))
                    components.append(edge_stack[ind:])
                    # no need to empty stack until ind because the next component
                    # will start from top of stack, i.e., after ind

    return components


def get_n_biconnected_component(pn):
    components = get_biconnected_component_list(pn)
    return len(components)


