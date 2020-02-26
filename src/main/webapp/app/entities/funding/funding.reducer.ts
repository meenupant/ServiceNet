import axios from 'axios';
import { ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IFunding, defaultValue } from 'app/shared/model/funding.model';
import { SERVICENET_API_URL } from 'app/shared/util/service-url.constants';

export const ACTION_TYPES = {
  FETCH_FUNDING_LIST: 'funding/FETCH_FUNDING_LIST',
  FETCH_FUNDING: 'funding/FETCH_FUNDING',
  CREATE_FUNDING: 'funding/CREATE_FUNDING',
  UPDATE_FUNDING: 'funding/UPDATE_FUNDING',
  DELETE_FUNDING: 'funding/DELETE_FUNDING',
  RESET: 'funding/RESET'
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IFunding>,
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false
};

export type FundingState = Readonly<typeof initialState>;

// Reducer

export default (state: FundingState = initialState, action): FundingState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_FUNDING_LIST):
    case REQUEST(ACTION_TYPES.FETCH_FUNDING):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true
      };
    case REQUEST(ACTION_TYPES.CREATE_FUNDING):
    case REQUEST(ACTION_TYPES.UPDATE_FUNDING):
    case REQUEST(ACTION_TYPES.DELETE_FUNDING):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true
      };
    case FAILURE(ACTION_TYPES.FETCH_FUNDING_LIST):
    case FAILURE(ACTION_TYPES.FETCH_FUNDING):
    case FAILURE(ACTION_TYPES.CREATE_FUNDING):
    case FAILURE(ACTION_TYPES.UPDATE_FUNDING):
    case FAILURE(ACTION_TYPES.DELETE_FUNDING):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload
      };
    case SUCCESS(ACTION_TYPES.FETCH_FUNDING_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
        totalItems: action.payload.headers['x-total-count']
      };
    case SUCCESS(ACTION_TYPES.FETCH_FUNDING):
      return {
        ...state,
        loading: false,
        entity: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.CREATE_FUNDING):
    case SUCCESS(ACTION_TYPES.UPDATE_FUNDING):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.DELETE_FUNDING):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {}
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState
      };
    default:
      return state;
  }
};

const apiUrl = SERVICENET_API_URL + '/fundings';

// Actions

export const getEntities: ICrudGetAllAction<IFunding> = (page, size, sort) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return {
    type: ACTION_TYPES.FETCH_FUNDING_LIST,
    payload: axios.get<IFunding>(`${requestUrl}${sort ? '&' : '?'}cacheBuster=${new Date().getTime()}`)
  };
};

export const getEntity: ICrudGetAction<IFunding> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_FUNDING,
    payload: axios.get<IFunding>(requestUrl)
  };
};

export const createEntity: ICrudPutAction<IFunding> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_FUNDING,
    payload: axios.post(apiUrl, cleanEntity(entity))
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IFunding> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_FUNDING,
    payload: axios.put(apiUrl, cleanEntity(entity))
  });
  dispatch(getEntities());
  return result;
};

export const deleteEntity: ICrudDeleteAction<IFunding> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_FUNDING,
    payload: axios.delete(requestUrl)
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET
});
