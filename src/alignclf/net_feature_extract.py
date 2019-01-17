#!/usr/bin/env python


import podspy
import podspy.petrinet


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


def get_n_inv_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    inv_tran = filter(lambda t: t.is_invisible(), pn.transitions)
    return len(list(inv_tran))


def get_n_dup_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    tran_dict = {}
    for t in pn.transitions:
        cnt = tran_dict.get(t.label, default=0)
        tran_dict[t.label] = cnt + 1
    dup_tran = filter(lambda t_label: tran_dict[t_label] > 1, tran_dict.keys())
    return len(list(dup_tran))


def get_n_uniq_tran(pn):
    assert isinstance(pn, podspy.petrinet.Petrinet)
    tran_dict = {}
    for t in pn.transitions:
        cnt = tran_dict.get(t.label, default=0)
        tran_dict[t.label] = cnt + 1
    uniq_tran = filter(lambda t_label: tran_dict[t_label] == 1, tran_dict.keys())
    return len(list(uniq_tran))


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



